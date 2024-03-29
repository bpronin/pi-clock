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
    ContentControlAdapter(settings) {
    
    private val gestureDetector by lazy {
        GestureDetectorCompat(requireContext(), object : SimpleOnGestureListener() {

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!swiped) {
                    scaleFactor =
                        1.5f / view.parentView.scaledRect.height() /* to 2/3 of parents height */
                    swiped = true
                    onSwipeStart()
                } else {
                    val alpha = view.alpha + distanceY * scaleFactor
                    view.alpha = min(MAX_ALPHA, max(alpha, MIN_ALPHA))
                    onSwipe(toPercents(view.alpha))
                }
                return true
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
    private var swiped = false
    private var scaleFactor = 0f
    private var savedAlpha = MIN_ALPHA

    lateinit var onSwipeStart: () -> Unit
    lateinit var onSwipe: (brightness: Int) -> Unit
    lateinit var onSwipeEnd: () -> Unit

    init {
        loadGesturesState()
        loadBrightness()
    }

    private fun fade(alpha: Float, onEnd: () -> Unit = {}) {
        if (view.alpha == alpha) return

        Log.v(TAG, "Start fade to: $alpha")

        fadeAnimator.apply {
            cancel()
            removeAllListeners()

            addListener(object : AnimatorListenerAdapter() {
                var canceled = false

                override fun onAnimationCancel(animation: Animator?) {
                    Log.v(TAG, "Canceled fade")

                    canceled = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (canceled) return

                    Log.v(TAG, "End fade")

                    onEnd()
                }
            })

            setFloatValues(view.alpha, alpha)

            start()
        }
    }

    private fun updateViewAlpha() {
        view.alpha = if (mode == MODE_INACTIVE || mode == MODE_EDITOR) {
            savedAlpha
        } else {
            MAX_ALPHA
        }

        Log.v(TAG, "View alpha set to: ${view.alpha}")
    }

    private fun loadGesturesState() {
        gesturesEnabled = settings.getBoolean(PREF_GESTURES_ENABLED)
    }

    private fun loadBrightness() {
        savedAlpha = toDecimal(settings.getInt(PREF_MUTED_BRIGHTNESS))
        updateViewAlpha()

        Log.v(TAG, "Muted alpha set to: $savedAlpha")
    }

    private fun saveBrightness() {
        savedAlpha = view.alpha
        settings.update { putInt(PREF_MUTED_BRIGHTNESS, toPercents(savedAlpha)) }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_MUTED_BRIGHTNESS -> loadBrightness()
            PREF_GESTURES_ENABLED -> loadGesturesState()
        }
    }

    override fun onModeChanged(animate: Boolean) {
        if (animate) {
            when (mode) {
                MODE_ACTIVE -> fade(MAX_ALPHA) { updateViewAlpha() }
                MODE_INACTIVE,
                MODE_EDITOR -> fade(savedAlpha) { updateViewAlpha() }
            }
        } else {
            updateViewAlpha()
        }
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (gesturesEnabled && (mode == MODE_INACTIVE || mode == MODE_ACTIVE)) {
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                ACTION_DOWN -> swiped = false
                ACTION_UP -> {
                    if (swiped) {
                        saveBrightness()
                        onSwipeEnd()
                        return true
                    }
                }
            }
        }

        return false
    }

    companion object {

        private const val TAG = "BrightnessControl"

        private const val SYSTEM_BAR_SWIPE_ZONE = 50

        const val MIN_BRIGHTNESS = 10
        const val MAX_BRIGHTNESS = 100

        private val MIN_ALPHA = toDecimal(MIN_BRIGHTNESS)
        private val MAX_ALPHA = toDecimal(MAX_BRIGHTNESS)
    }

}
