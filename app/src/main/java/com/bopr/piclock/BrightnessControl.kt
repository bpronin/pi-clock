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
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(private val view: View) :
    GestureDetector.SimpleOnGestureListener() {

    private val _tag = "BrightnessControl"

    private val detector = GestureDetectorCompat(view.context, this)
    private val scaleFactor = 10f // todo: make it depends on vertical screen size
    private var scrollingBrightness = MIN_BRIGHTNESS
    private var scrolled = false

    private val brightnessAnimator by lazy {
        ObjectAnimator().apply {
            target = view
            setProperty(View.ALPHA)
            duration = 2000
            interpolator = AccelerateInterpolator()
        }
    }

    private var inactiveBrightness: Int = MIN_BRIGHTNESS

    private var viewBrightness: Int
        get() = (view.alpha * 100).toInt()
        set(value) {
            view.alpha = value / 100f

//            Log.v(_tag, "View alpha set to: ${view.alpha}")
        }

    lateinit var onStartSlide: () -> Unit
    lateinit var onSlide: (value: Int) -> Unit
    lateinit var onEndSlide: (value: Int) -> Unit

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (!scrolled) {
            scrollingBrightness = viewBrightness
            onStartSlide()
        } else {
            scrollingBrightness += (distanceY / scaleFactor).toInt()
            scrollingBrightness = min(MAX_BRIGHTNESS, max(scrollingBrightness, MIN_BRIGHTNESS))
            viewBrightness = scrollingBrightness
            onSlide(scrollingBrightness)
        }
        scrolled = true
        return false
    }

    private fun fadeBrightness(value: Int, onEnd: () -> Unit = {}) {
        Log.v(_tag, "Start fade to:$value")

        brightnessAnimator.apply {
            cancel()
            removeAllListeners()

            setFloatValues(viewBrightness / 100f, value / 100f)
            doOnEnd {
                Log.v(_tag, "End fade")

                onEnd()
            }

            start()
        }
    }

    private fun updateBrightness(mode: Int) {
        viewBrightness = if (mode == MODE_INACTIVE || mode == MODE_EDITOR)
            inactiveBrightness
        else
            MAX_BRIGHTNESS
    }

    fun setInactiveBrightness(value: Int, mode: Int) {
        inactiveBrightness = value
        updateBrightness(mode)
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
                if (scrolled) onEndSlide(scrollingBrightness)
                return scrolled
            }
        }

        return false
    }

    fun onModeChanged(mode: Int, animate: Boolean) {
        if (animate) {
            when (mode) {
                MODE_ACTIVE ->
                    fadeBrightness(MAX_BRIGHTNESS) { updateBrightness(mode) }
                MODE_INACTIVE, MODE_EDITOR ->
                    fadeBrightness(inactiveBrightness) { updateBrightness(mode) }
            }
        } else {
            updateBrightness(mode)
        }
    }

    companion object {

        private const val MIN_BRIGHTNESS = 10
        private const val MAX_BRIGHTNESS = 100
    }

}
