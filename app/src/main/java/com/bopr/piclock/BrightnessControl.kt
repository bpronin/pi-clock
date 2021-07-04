package com.bopr.piclock

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import androidx.core.view.GestureDetectorCompat
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control brightness by slide gesture.
 */
internal class BrightnessControl(context: Context) : GestureDetector.SimpleOnGestureListener() {

    var inactiveBrightness: Int = MIN_BRIGHTNESS
        set(value) {
            if (field != value) {
                field = value
                onChangeBrightness(inactiveBrightness, MAX_BRIGHTNESS)
            }
        }

    lateinit var onStartSlide: () -> Int
    lateinit var onSlide: (value: Int) -> Unit
    lateinit var onEndSlide: () -> Unit
    lateinit var onChangeBrightness: (inactiveValue: Int, maxValue: Int) -> Unit
    lateinit var onFadeBrightness: (value: Int) -> Unit

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
        if (animate) {
            if (newMode == MODE_ACTIVE && oldMode == MODE_INACTIVE) {
                onFadeBrightness(MAX_BRIGHTNESS)
            } else if (newMode == MODE_INACTIVE && oldMode == MODE_ACTIVE) {
                onFadeBrightness(inactiveBrightness)
            }
        }
        onChangeBrightness(inactiveBrightness, MAX_BRIGHTNESS)
    }

    companion object {

        private const val MIN_BRIGHTNESS = 10
        private const val MAX_BRIGHTNESS = 100
    }

}
