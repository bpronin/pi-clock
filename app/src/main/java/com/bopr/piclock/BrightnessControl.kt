package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.ALPHA
import android.view.animation.AccelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.scaledRect
import com.bopr.piclock.util.toDecimal
import com.bopr.piclock.util.toPercents
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control content view brightness.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class BrightnessControl(private val view: View, settings: Settings) :
    ContentControl(settings) {

    private val _tag = "BrightnessControl"
    private val gestureDetector by lazy {
        GestureDetectorCompat(requireContext(), object : SimpleOnGestureListener() {

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!swiping) {
                    scaleFactor =
                        1.5f / view.parentView.scaledRect.height() /* to 2/3 of parents height */
                    swiping = true
                    onSwipeStart()
                } else {
                    val alpha = view.alpha + distanceY * scaleFactor
                    view.alpha = min(MAX_ALPHA, max(alpha, MIN_ALPHA))
                    onSwipe(toPercents(view.alpha))
                }
                return false
            }
        })
    }
    private val fadeAnimator by lazy {
        ObjectAnimator().apply {
            target = view
            setProperty(ALPHA)
            duration = 1000
            interpolator = AccelerateInterpolator()
        }
    }

    private var gesturesEnabled = true
    private var scaleFactor = 0f
    private var mutedAlpha = MIN_ALPHA
    private var swiping = false

    lateinit var onSwipeStart: () -> Unit
    lateinit var onSwipe: (brightness: Int) -> Unit
    lateinit var onSwipeEnd: () -> Unit

    init {
        updateGesturesState()
        updateMutedAlpha()
    }

    private fun fade(alpha: Float, onEnd: () -> Unit = {}) {
        if (view.alpha == alpha) return

        Log.v(_tag, "Start fade to: $alpha")

        fadeAnimator.apply {
            cancel()
            removeAllListeners()

            addListener(object : AnimatorListenerAdapter() {
                var canceled = false

                override fun onAnimationCancel(animation: Animator?) {
                    Log.v(_tag, "Canceled fade")

                    canceled = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (canceled) return

                    Log.v(_tag, "End fade")

                    onEnd()
                }
            })

            setFloatValues(view.alpha, alpha)

            start()
        }
    }

    private fun updateViewAlpha() {
        view.alpha = if (mode == MODE_INACTIVE || mode == MODE_EDITOR) {
            mutedAlpha
        } else {
            MAX_ALPHA
        }

        Log.v(_tag, "View alpha set to: ${view.alpha}")
    }

    private fun updateGesturesState() {
        gesturesEnabled = settings.getBoolean(PREF_GESTURES_ENABLED)
    }

    private fun updateMutedAlpha() {
        mutedAlpha = toDecimal(settings.getInt(PREF_MUTED_BRIGHTNESS))
        updateViewAlpha()

        Log.v(_tag, "Muted alpha set to: $mutedAlpha")
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (gesturesEnabled && (mode == MODE_INACTIVE || mode == MODE_ACTIVE)) {
            gestureDetector.onTouchEvent(event)

            /* this is to prevent of calling onClick if scrolled */
            when (event.action) {
                ACTION_DOWN ->
                    swiping = false
                ACTION_UP -> {
                    if (swiping) {
                        settings.update {
                            putInt(PREF_MUTED_BRIGHTNESS, toPercents(view.alpha))
                        }
                        onSwipeEnd()
                    }
                    return swiping
                }
            }
        }

        return false
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_MUTED_BRIGHTNESS -> updateMutedAlpha()
            PREF_GESTURES_ENABLED -> updateGesturesState()
        }
    }

    override fun onModeChanged(@Mode newMode: Int, animate: Boolean) {
        super.onModeChanged(newMode, animate)
        if (animate) {
            when (newMode) {
                MODE_ACTIVE ->
                    fade(MAX_ALPHA) { updateViewAlpha() }
                MODE_INACTIVE, MODE_EDITOR ->
                    fade(mutedAlpha) { updateViewAlpha() }
            }
        } else {
            updateViewAlpha()
        }
    }

    companion object {

        const val MIN_BRIGHTNESS = 10
        const val MAX_BRIGHTNESS = 100

        private val MIN_ALPHA = toDecimal(MIN_BRIGHTNESS)
        private val MAX_ALPHA = toDecimal(MAX_BRIGHTNESS)
    }

}
