package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
            removeAllListeners() /*important. doOnStart(), doOnEnd() add! the listeners */
            target = view
            setup()
            start()
        }
    }

    fun showFab(view: View) {
        view.apply {
            alpha = 0f
            rotation = -90f
            visibility = VISIBLE
            animate()
                .setDuration(500)
                .alpha(1f)
                .rotation(0f)
                .setInterpolator(AccelerateInterpolator())
                .setListener(null)  /* important. see android.view.View.animate realisation */
                .start()
        }
    }

    fun hideFab(view: View, onEnd: () -> Unit = {}) {
        view.apply {
            visibility = VISIBLE
            alpha = 1f
            rotation = 0f
            animate()
                .setDuration(500)
                .alpha(0f)
                .rotation(-90f)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator?) {
                        visibility = INVISIBLE
                        onEnd()
                    }
                })
                .start()
        }
    }

    fun fadeInContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeInContentAnimator.play(view) {
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnStart { onStart(this) }
            doOnEnd { onEnd(this) }
        }
    }

    fun fadeOutContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeOutContentAnimator.play(view) {
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
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