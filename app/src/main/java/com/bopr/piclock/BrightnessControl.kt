package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.ALPHA
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.GestureDetectorCompat
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.scaledRect
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(private val view: View) :
    GestureDetector.SimpleOnGestureListener() {

    private val _tag = "BrightnessControl"

    private val detector = GestureDetectorCompat(view.context, this)
    private var scaleFactor = 0f
    private var slidingAlpha = 0f
    private var mutedAlpha = MIN_ALPHA
    private var sliding = false

    private val fadeAnimator by lazy {
        ObjectAnimator().apply {
            target = view
            setProperty(ALPHA)
            duration = 2000
            interpolator = AccelerateInterpolator()
        }
    }
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
        Log.v(_tag, "Start fade to: $alpha")

        fadeAnimator.apply {
            cancel()
            removeAllListeners()

            setFloatValues(view.alpha, alpha)
            doOnEnd {
                Log.v(_tag, "End fade")

                onEnd()
            }

            start()
        }
    }

    private fun updateViewAlpha(mode: Int) {
        view.alpha = if (mode == MODE_INACTIVE || mode == MODE_EDITOR) mutedAlpha else MAX_ALPHA
    }

    fun setMutedBrightness(brightness: Int, mode: Int) {
        mutedAlpha = alpha(brightness)
        updateViewAlpha(mode)
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent, mode: Int): Boolean {
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

    fun onModeChanged(mode: Int, animate: Boolean) {
        if (animate) {
            when (mode) {
                MODE_ACTIVE ->
                    fade(MAX_ALPHA) { updateViewAlpha(mode) }
                MODE_INACTIVE, MODE_EDITOR ->
                    fade(mutedAlpha) { updateViewAlpha(mode) }
            }
        } else {
            updateViewAlpha(mode)
        }
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
