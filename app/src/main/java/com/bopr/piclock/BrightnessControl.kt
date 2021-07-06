package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.ALPHA
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.scaledRect
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl() :
    GestureDetector.SimpleOnGestureListener() {

    private val _tag = "BrightnessControl"

    private val detector by lazy {
        GestureDetectorCompat(view.context, this)
    }

    private val fadeAnimator by lazy {
        ObjectAnimator().apply {
            target = view
            setProperty(ALPHA)
            duration = 1000
            interpolator = AccelerateInterpolator()
        }
    }

    private lateinit var view: View
    private var scaleFactor = 0f
    private var slidingAlpha = 0f
    private var mutedAlpha = MIN_ALPHA
    private var sliding = false

    @Mode
    private var mode: Int = MODE_INACTIVE

    lateinit var onStartSlide: () -> Unit
    lateinit var onSlide: (brightness: Int) -> Unit
    lateinit var onEndSlide: (brightness: Int) -> Unit

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!sliding) {
            scaleFactor = 1.5f / view.parentView.scaledRect.height() /* to 2/3 of parents height */
            slidingAlpha = view.alpha
            onStartSlide()
            sliding = true
        } else {
            slidingAlpha += distanceY * scaleFactor
            slidingAlpha = min(MAX_ALPHA, max(slidingAlpha, MIN_ALPHA))
            view.alpha = slidingAlpha
            onSlide(brightness(slidingAlpha))
        }
        return false
    }

    private fun fade(alpha: Float, onEnd: () -> Unit = {}) {
        if (view.alpha == alpha) return

        Log.v(_tag, "Start fade to: $alpha")

        fadeAnimator.apply {
            cancel()
            removeAllListeners()

            addListener(object : AnimatorListenerAdapter() {
                var canceled = false;

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
        view.alpha = if (mode == MODE_INACTIVE || mode == MODE_EDITOR) mutedAlpha else MAX_ALPHA

        Log.v(_tag, "View alpha set to: ${view.alpha}")
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (mode == MODE_INACTIVE || mode == MODE_ACTIVE) {
            detector.onTouchEvent(event)

            /* this is to prevent of calling onClick if scrolled */
            when (event.action) {
                ACTION_DOWN ->
                    sliding = false
                ACTION_UP -> {
                    if (sliding) onEndSlide(brightness(slidingAlpha))
                    return sliding
                }
            }
        }

        return false
    }

    fun onViewCreated(view: ViewGroup) {
        this.view = view
    }

    fun onModeChanged(@Mode mode: Int, animate: Boolean) {
        this.mode = mode

        if (animate) {
            when (mode) {
                MODE_ACTIVE ->
                    fade(MAX_ALPHA) { updateViewAlpha() }
                MODE_INACTIVE, MODE_EDITOR ->
                    fade(mutedAlpha) { updateViewAlpha() }
            }
        } else {
            updateViewAlpha()
        }
    }

    fun setMutedBrightness(brightness: Int) {
        mutedAlpha = alpha(brightness)

        Log.d(_tag, "Muted alpha set to: $mutedAlpha")

        updateViewAlpha()
    }

    companion object {

        private fun alpha(brightness: Int) = brightness / 100f

        private fun brightness(alpha: Float) = (alpha * 100).toInt()

        const val MIN_BRIGHTNESS = 10
        const val MAX_BRIGHTNESS = 100

        private val MIN_ALPHA = alpha(MIN_BRIGHTNESS)
        private val MAX_ALPHA = alpha(MAX_BRIGHTNESS)
    }

}
