package com.bopr.piclock

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.core.view.GestureDetectorCompat

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(context: Context) : GestureDetector.SimpleOnGestureListener() {

    lateinit var onStartSlide: () -> Int
    lateinit var onEndSlide: () -> Unit
    lateinit var onSlide: (delta: Int) -> Unit

    private val detector = GestureDetectorCompat(context, this)
    private var scrolled = false
    private var delta = 0
    private val factor = 10f // todo: make it depends from vertical screen size

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!scrolled) {
            delta = onStartSlide()
        } else {
            delta += (distanceY / factor).toInt()
            onSlide(delta)
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
