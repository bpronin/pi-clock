package com.bopr.piclock

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector

/**
 * Convenience class to control scale by pinch gesture.
 */
internal class ScaleControl(context: Context) : ScaleGestureDetector.OnScaleGestureListener {

    lateinit var onPinchStart: () -> Float
    lateinit var onPinchEnd: () -> Unit
    lateinit var onPinch: (Float) -> Unit

    private val detector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var pinched = false
    private var factor = 0f

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        pinched = true
        factor = onPinchStart()
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        factor *= detector.scaleFactor
        onPinch(factor)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        onPinchEnd()
    }

    /**
     * To be called in owner's onTouch.
     */
    fun processTouch(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)

        /* this is to prevent of calling onClick if pinched */
        when (event.action) {
            ACTION_DOWN ->
                pinched = false
            ACTION_UP ->
                return pinched
        }

        return false
    }

}
