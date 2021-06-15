package com.bopr.piclock

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

internal class ClockFragmentScaleControl(
    context: Context,
    controllerView: View,
    private val controlView: View,
) : ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

    /** Logger tag. */
    private val _tag = "ClockFragmentScaleControl"

    private val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)

    var onChanged: (scale: Float) -> Unit = {}

    var onEnd: (scale: Float) -> Unit = {}
    var factor = 1f
        set(value) {
            if (field != value) {
                field = value
                updateControlView()
            }
        }

    private var scaled = false

    init {
        controllerView.setOnTouchListener(this)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        scaled = true
        Log.d(_tag, "BEGIN ")
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val f = factor * detector.scaleFactor
        factor = max(0.2f, min(f, 2.0f))
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(_tag, "END ")
        onEnd(factor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(controllerView: View?, event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)

        /* this is to prevent of calling onClick after scaling */
        when (event?.action) {
            ACTION_DOWN -> scaled = false
            ACTION_UP -> return scaled
        }

        return false
    }

    private fun updateControlView() {
        controlView.apply {
            scaleX = factor
            scaleY = factor
        }
    }

}
