package com.bopr.piclock

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.core.view.GestureDetectorCompat
import kotlin.math.max

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(context: Context) : GestureDetector.SimpleOnGestureListener() {

    lateinit var onStartSlide: () -> Int
    lateinit var onSlide: (value: Int) -> Unit
    lateinit var onEndSlide: () -> Unit

    private val minValue = 10
    private var value = 0
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
            value = onStartSlide()
        } else {
            value += (distanceY / scaleFactor).toInt()
            value = max(value, minValue)
            onSlide(value)
        }
        scrolled = true
        return false
    }

    /**
     * To be called in owner's onTouch.
     */
    fun processTouch(event: MotionEvent): Boolean {
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

}
