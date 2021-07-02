package com.bopr.piclock

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import com.bopr.piclock.util.getStringArray
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control scale.
 */
internal class ScaleControl(context: Context) : ScaleGestureDetector.OnScaleGestureListener {

    private val _tag = "ScaleControl"

    lateinit var onPinchStart: () -> Float
    lateinit var onPinch: (Float) -> Unit
    lateinit var onPinchEnd: () -> Unit

    private val detector = ScaleGestureDetector(context, this)
    private val minScaleFactor = context.getStringArray(R.array.scale_values).first().toFloat()
    private var factor = 0f
    private var pinched = false

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        pinched = true
        factor = onPinchStart()
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        factor *= detector.scaleFactor
        factor = max(minScaleFactor, min(factor, MAX_SCALE_FACTOR))
        onPinch(factor)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        onPinchEnd()
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
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

    companion object {

        private const val MAX_SCALE_FACTOR = 100f
    }

}
