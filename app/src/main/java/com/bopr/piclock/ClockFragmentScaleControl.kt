package com.bopr.piclock

import android.content.Context
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

internal class ClockFragmentScaleControl(
    context: Context,
    private val controllingView: View
) : ScaleGestureDetector.OnScaleGestureListener {

    private val minFactor = context.resources.getStringArray(R.array.scale_values).first().toFloat()
    private val maxFactor = context.resources.getStringArray(R.array.scale_values).last().toFloat()
    private val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var changed = false

    var onEnd: (scale: Float) -> Unit = {}
    var factor = 0f
        set(value) {
            if (field != value) {
                field = value
                updateControlView()
            }
        }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        changed = true
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val f = factor * detector.scaleFactor
        factor = max(minFactor, min(f, maxFactor))
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        onEnd(factor)
    }

    fun onTouch(event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)

        /* this is to prevent of calling onClick after scaling */
        when (event?.action) {
            ACTION_DOWN -> changed = false
            ACTION_UP -> return changed
        }

        return false
    }

    private fun updateControlView() {
        //todo: fix position (move into screen) if new size does not fit
        controllingView.apply {
            scaleX = factor
            scaleY = factor
        }
    }

}
