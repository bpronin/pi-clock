package com.bopr.piclock

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.core.view.GestureDetectorCompat
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(context: Context) : GestureDetector.SimpleOnGestureListener() {

    lateinit var onStartSlide: () -> Int
    lateinit var onSlide: (value: Int) -> Unit
    lateinit var onEndSlide: () -> Unit

    private var brightness = 0  //todo: do let be less tah min
    private val detector = GestureDetectorCompat(context, this)
    private var scrolled = false
    private val scaleFactor = 10f // todo: make it depends on vertical screen size

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!scrolled) {
            brightness = onStartSlide()
        } else {
            brightness += (distanceY / scaleFactor).toInt()
            brightness = min(MAX_BRIGHTNESS, max(brightness, MIN_BRIGHTNESS))
            onSlide(brightness)
        }
        scrolled = true
        return false
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)

        /* this is to prevent of calling onClick if scrolled */
        when (event.action) {
            ACTION_DOWN -> scrolled = false
            ACTION_UP -> {
                if (scrolled) onEndSlide()
                return scrolled
            }
        }

        return false
    }

    fun onModeChanged(oldMode: Int, newMode: Int, animate: Boolean) {
        updateBrightness(newMode)
    }

    private fun updateBrightness(mode: Int) {
//        currentBrightness = if (mode == MODE_INACTIVE)
//            inactiveBrightness
//        else
//            BrightnessControl.MAX_BRIGHTNESS
    }

    companion object {

        private const val MIN_BRIGHTNESS = 10
        const val MAX_BRIGHTNESS = 100
    }

}
