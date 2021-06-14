package com.bopr.piclock

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

internal class ClockFragmentAnimations {

    private val fabShowAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            setFloatValues(0f, 1f)
            duration = 500L
            interpolator = AccelerateInterpolator()
        }
    }

    private val fabHideAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            setFloatValues(1f, 0f)
            duration = 500L
            interpolator = AccelerateInterpolator()
        }
    }

    private val fadeInContentAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            duration = 2000L
            interpolator = AccelerateInterpolator()
        }
    }

    private val fadeOutContentAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            duration = 2000L
            interpolator = AccelerateInterpolator()
        }
    }

    private val timeSeparatorAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            setFloatValues(0f, 1f)
            duration = 2000L
            interpolator = CycleInterpolator(1f)
            doOnCancel { }
        }
    }

    private val secondsSeparatorAnimator by lazy {
        addAnimator {
            setPropertyName("alpha")
            setFloatValues(0f, 1f)
            duration = 2000L
            interpolator = CycleInterpolator(1f)
        }
    }

    private val animators = mutableSetOf<Animator>()

    private inline fun addAnimator(setup: ObjectAnimator.() -> Unit) =
        ObjectAnimator().apply {
            setup()
            animators.add(this)
        }

    private inline fun ObjectAnimator.play(view: View, setup: ObjectAnimator.() -> Unit = {}) {
        apply {
            cancel()
            target = view
            setup()
            start()
        }
    }

    fun stop() {
        for (animator in animators) {
            if (animator.isStarted) {
                animator.end()
            }
        }
    }

    fun showFab(view: View) {
        fabShowAnimator.play(view) {
            doOnStart { view.visibility = VISIBLE }
        }
    }

    fun hideFab(view: View) {
        fabHideAnimator.play(view) {
            doOnEnd { view.visibility = INVISIBLE }
        }
    }

    fun fadeInContent(
        view: View,
        brightness: Float,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeInContentAnimator.play(view) {
            setFloatValues(brightness, 1f)
            doOnStart { onStart(this) }
            doOnEnd { onEnd(this) }
        }
    }

    fun fadeOutContent(
        view: View,
        brightness: Float,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeOutContentAnimator.play(view) {
            setFloatValues(1f, brightness)
            doOnStart { onStart(this) }
            doOnEnd { onEnd(this) }
        }
    }

    fun blinkTimeSeparator(view: View) {
        timeSeparatorAnimator.play(view)
    }

    fun blinkSecondsSeparator(view: View) {
        secondsSeparatorAnimator.play(view)
    }

}