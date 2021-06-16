package com.bopr.piclock

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlin.math.max
import kotlin.math.min

internal class ClockFragmentBrightnessControl(
    context: Context,
    private val controllingView: View
) : GestureDetector.SimpleOnGestureListener() {

    private val gestureDetector = GestureDetectorCompat(context, this)

    var brightness = 0
        private set(value) {
            if (field != value) {
                field = value
                controllingView.alpha = field / 100f
            }
        }
    var minBrightness = 0
        set(value) {
            if (field != value) {
                field = value
                brightness = field
            }
        }
    val maxBrightness = 100
    var onEnd: (brightness: Int) -> Unit = {}

    private var changed = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val b = brightness + (distanceY / 10f).toInt()
        brightness = max(10, min(b, maxBrightness))
        changed = true
        return false
    }

    fun onTouch(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event?.action) {
            ACTION_DOWN -> changed = false
            ACTION_UP -> {
                if (changed) {
                    onEnd(brightness)
                }
                return changed
            }
        }

        return false
    }

    fun setMaxBrightness() {
        brightness = maxBrightness
    }

    fun setMinBrightness() {
        brightness = minBrightness
    }

}
