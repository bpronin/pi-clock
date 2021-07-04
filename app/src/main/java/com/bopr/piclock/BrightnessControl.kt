package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
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
    private var scrollingBrightness = MIN_BRIGHTNESS
    private var inactiveBrightness = MIN_BRIGHTNESS
    private var scrolled = false

    private val brightnessAnimator by lazy {
        ObjectAnimator().apply {
            target = view
            setProperty(View.ALPHA)
            duration = 2000
            interpolator = AccelerateInterpolator()
        }
    }

    lateinit var onStartSlide: () -> Unit
    lateinit var onSlide: (value: Int) -> Unit
    lateinit var onEndSlide: (value: Int) -> Unit

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!scrolled) {
            /* set factor to 2/3 of parents height */
            scaleFactor = 1.5f * (1.0f - MIN_BRIGHTNESS) / view.parentView.scaledRect.height()

            scrollingBrightness = view.alpha
            onStartSlide()
        } else {
            scrollingBrightness += distanceY * scaleFactor
            scrollingBrightness = min(MAX_BRIGHTNESS, max(scrollingBrightness, MIN_BRIGHTNESS))
            view.alpha = scrollingBrightness
            onSlide((scrollingBrightness * 100).toInt())
        }
        scrolled = true
        return false
    }

    private fun fadeBrightness(value: Float, onEnd: () -> Unit = {}) {
        Log.v(_tag, "Start fade to: $value")

        brightnessAnimator.apply {
            cancel()
            removeAllListeners()

            setFloatValues(view.alpha, value)
            doOnEnd {
                Log.v(_tag, "End fade")

                onEnd()
            }

            start()
        }
    }

    private fun updateViewBrightness(mode: Int) {
        view.alpha = if (mode == MODE_INACTIVE || mode == MODE_EDITOR)
            inactiveBrightness
        else
            MAX_BRIGHTNESS
    }

    fun setInactiveBrightness(value: Int, mode: Int) {
        inactiveBrightness = value / 100f
        updateViewBrightness(mode)
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)

        /* this is to prevent of calling onClick if scrolled */
        when (event.action) {
            ACTION_DOWN ->
                scrolled = false
            ACTION_UP -> {
                if (scrolled) onEndSlide((scrollingBrightness * 100).toInt())
                return scrolled
            }
        }

        return false
    }

    fun onModeChanged(mode: Int, animate: Boolean) {
        if (animate) {
            when (mode) {
                MODE_ACTIVE ->
                    fadeBrightness(MAX_BRIGHTNESS) { updateViewBrightness(mode) }
                MODE_INACTIVE, MODE_EDITOR ->
                    fadeBrightness(inactiveBrightness) { updateViewBrightness(mode) }
            }
        } else {
            updateViewBrightness(mode)
        }
    }

    companion object {

        private const val MIN_BRIGHTNESS = 0.1f
        private const val MAX_BRIGHTNESS = 1.0f
    }

}
