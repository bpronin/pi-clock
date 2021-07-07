package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.CycleInterpolator
import java.util.*

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

    private fun resetAlpha() {
        minutesSeparator.alpha = 1f
        secondsSeparator.alpha = 1f
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == INVISIBLE) VISIBLE else INVISIBLE
    }

    private fun toggleVisibility(time: Date) {
        resetAlpha()
        toggleVisibility(minutesSeparator)
        if (secondsEnabled) {
            toggleVisibility(secondsSeparator)
        }
    }

    private fun startAnimators(time: Date) {
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

    fun onTimer(time: Date) {
        if (!enabled) return

        if (animated) {
            startAnimators(time)
        } else {
            toggleVisibility(time)
        }
    }

    fun setAnimated(animated: Boolean) {
        if (this.animated != animated) {
            this.animated = animated
            resetAlpha()
        }
    }

    fun setEnabled(enabled: Boolean) {
        if (this.enabled != enabled) {
            this.enabled = enabled

            Log.d(_tag, "Enabled: $enabled")

            if (!this.enabled) {
                minutesSeparatorAnimator.end()
                secondsSeparatorAnimator.end()
                resetAlpha() /* because we are using Cycling interpolator */
            }
        }
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
