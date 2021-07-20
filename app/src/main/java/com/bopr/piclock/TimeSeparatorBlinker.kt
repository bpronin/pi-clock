package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.animation.CycleInterpolator
import androidx.core.view.isVisible

/**
 * Controls time separators blinking in digital clock.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class TimeSeparatorBlinker(
    private val minutesSeparator: View,
    private val secondsSeparator: View
) {

    private val minutesSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(View.ALPHA)
            setFloatValues(0f, 1f)
            target = minutesSeparator
            duration = 2000
            interpolator = CycleInterpolator(1f)
        }
    }

    private val secondsSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(View.ALPHA)
            setFloatValues(0f, 1f)
            target = secondsSeparator
            duration = 2000
            interpolator = CycleInterpolator(1f)
        }
    }

    private var animated = true
    private var enabled = true
    private var secondsEnabled = true

    private fun toggleVisibility(halfTick: Int) {
        resetAlpha()

        val visible = halfTick % 2 == 0

        minutesSeparator.isVisible = visible
        secondsSeparator.isVisible = secondsEnabled && visible
    }

    private fun startAnimators(halfTick: Int) {
        if (halfTick % 4 != 0) return

        resetVisibility()

        minutesSeparatorAnimator.apply {
            end()
            start()
        }

        if (secondsEnabled) {
            secondsSeparatorAnimator.apply {
                end()
                start()
            }
        }
    }

    private fun resetVisibility() {
        minutesSeparator.isVisible = true
        secondsSeparator.isVisible = true
    }

    private fun resetAlpha() {
        minutesSeparator.alpha = 1f
        secondsSeparator.alpha = 1f
    }

    fun onTimer(tick: Int) {
        if (!enabled) return

        if (animated) {
            startAnimators(tick)
        } else {
            toggleVisibility(tick)
        }
    }

    fun setAnimated(value: Boolean) {
        animated = value
    }

    fun setEnabled(value: Boolean) {
        if (enabled != value) {
            enabled = value

            Log.d(TAG, "Enabled: $value")

            if (!enabled) {
                minutesSeparatorAnimator.end()
                secondsSeparatorAnimator.end()
                resetAlpha() /* because we are using Cycling interpolator */
            }
        }
    }

    fun setSecondsEnabled(enabled: Boolean) {
        if (secondsEnabled != enabled) {
            secondsEnabled = enabled

            Log.d(TAG, "Seconds enabled: $secondsEnabled")

            if (!secondsEnabled) {
                secondsSeparatorAnimator.end()
            }
        }
    }

    companion object {

        private const val TAG = "TimeSeparatorBlinker"
    }
}
