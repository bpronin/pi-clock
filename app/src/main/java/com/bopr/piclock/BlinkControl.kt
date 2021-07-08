package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.CycleInterpolator

/**
 * Convenience class to control time separators blinking.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class BlinkControl(
    private val minutesSeparator: View,
    private val secondsSeparator: View
) {

    private val _tag = "BlinkControl"

    private var animated = true
    private var enabled = true
    private var secondsEnabled = true

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

    private fun toggleVisibility(halfTick: Int) {
        resetAlpha()

        val visible = halfTick % 2 == 0

        minutesSeparator.visibility = if (visible) VISIBLE else INVISIBLE
        if (secondsEnabled) {
            secondsSeparator.visibility = if (visible) VISIBLE else INVISIBLE
        }
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

    fun onTimer(halfTick: Int) {
        if (!enabled) return

        if (animated) {
            startAnimators(halfTick)
        } else {
            toggleVisibility(halfTick)
        }
    }

    fun setAnimated(value: Boolean) {
        animated = value
    }

    fun setEnabled(value: Boolean) {
        if (enabled != value) {
            enabled = value

            Log.d(_tag, "Enabled: $value")

            if (!enabled) {
                minutesSeparatorAnimator.end()
                secondsSeparatorAnimator.end()
                resetAlpha() /* because we are using Cycling interpolator */
            }
        }
    }

    private fun resetVisibility() {
        minutesSeparator.visibility = VISIBLE
        secondsSeparator.visibility = VISIBLE
    }

    private fun resetAlpha() {
        minutesSeparator.alpha = 1f
        secondsSeparator.alpha = 1f
    }

    fun setSecondsEnabled(enabled: Boolean) {
        if (secondsEnabled != enabled) {
            secondsEnabled = enabled

            Log.d(_tag, "Seconds enabled: $secondsEnabled")

            if (!secondsEnabled) {
                secondsSeparatorAnimator.end()
            }
        }
    }
}
