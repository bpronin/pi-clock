package com.bopr.piclock

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
internal class BlinkControl(blinkingViews: Collection<View>) {

    private val animator by lazy {
        AnimatorSet().apply {
            duration = 2000
            interpolator = CycleInterpolator(1f)
            playTogether(views.map { createViewAnimator(it) })
        }
    }

    private val views = blinkingViews
    private var animated = true
    private var enabled = true

    private fun createViewAnimator(view: View) = ObjectAnimator().apply {
        setProperty(View.ALPHA)
        setFloatValues(0f, 1f)
        target = view
    }

    private fun resetAlpha() {
        for (view in views) view.alpha = 1f
    }

    private fun toggleVisibility(time: Date) {
        for (view in views) {
            view.visibility = if (view.visibility == INVISIBLE) VISIBLE else INVISIBLE
        }
    }

    private fun startAnimator(time: Date) {
        animator.apply {
            end()
            start()
        }
    }

    fun onTimer(time: Date) {
        if (!enabled) return

        if (animated) {
            startAnimator(time)
        } else {
            toggleVisibility(time)
        }
    }

    fun setAnimated(animated: Boolean) {
        this.animated = animated
        resetAlpha()
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!this.enabled) {
            animator.end()
            resetAlpha() /* because we are using Cycling interpolator */
        }
    }
}
