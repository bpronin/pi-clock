package com.bopr.piclock

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

internal class ClockFragmentAnimations {

    private val animators = mutableSetOf<Animator>()

    private val fabShowAnimator = createAnimator {
        setPropertyName("alpha")
        setFloatValues(0f, 1f)
        duration = 1000L
        interpolator = AccelerateInterpolator()
    }

    private val fabHideAnimator = createAnimator {
        setPropertyName("alpha")
        setFloatValues(1f, 0f)
        duration = 1000L
        interpolator = AccelerateInterpolator()
    }

    private val fadeInContentAnimator = createAnimator {
        setPropertyName("alpha")
        duration = 1000L
        interpolator = AccelerateInterpolator()
    }

    private val fadeOutContentAnimator = createAnimator {
        setPropertyName("alpha")
        duration = 1000L
        interpolator = AccelerateInterpolator()
    }

    private val timeSeparatorAnimator = createAnimator {
        setPropertyName("alpha")
        setFloatValues(0f, 1f)
        duration = 2000L
        interpolator = CycleInterpolator(1f)
    }

    private val secondsSeparatorAnimator = createAnimator {
        setPropertyName("alpha")
        setFloatValues(0f, 1f)
        duration = 2000L
        interpolator = CycleInterpolator(1f)
    }

    private fun createAnimator(initialize: ObjectAnimator.() -> Unit) = ObjectAnimator()
        .apply(initialize).also { animators.add(it) }

    private fun starAnimator(
        view: View,
        animator: ObjectAnimator,
        initialize: ObjectAnimator.() -> Unit = {}
    ) {
        animator.apply {
            cancel()
            target = view
            initialize()
            start()
        }
    }

    fun cancel() {
        for (animator in animators) {
            animator.cancel()
        }
    }

    fun showFab(view: View) {
        starAnimator(view, fabShowAnimator) {
            doOnStart { view.visibility = VISIBLE }
        }
    }

    fun hideFab(view: View) {
        starAnimator(view, fabHideAnimator) {
            doOnEnd { view.visibility = INVISIBLE }
        }
    }

    fun fadeInContent(
        view: View,
        brightnessPercents: Int,
        onStart: (Animator) -> Unit = {}
    ) {
        starAnimator(view, fadeInContentAnimator) {
            setFloatValues(brightnessPercents / 100f, 1f)
            doOnStart { onStart(this) }
        }
    }

    fun fadeOutContent(
        view: View, brightnessPercents: Int,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        starAnimator(view, fadeOutContentAnimator) {
            setFloatValues(1f, brightnessPercents / 100f)
            doOnStart { onStart(this) }
            doOnEnd { onEnd(this) }
        }
    }

    fun blinkTimeSeparator(view: View) {
        starAnimator(view, timeSeparatorAnimator)
    }

    fun blinkSecondsSeparator(view: View) {
        starAnimator(view, secondsSeparatorAnimator)
    }

}