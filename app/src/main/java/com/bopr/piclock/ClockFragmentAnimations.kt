package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

internal class ClockFragmentAnimations {

    private val fabShowAnimator by lazy {
        AnimatorSet().apply {
            duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(
                ObjectAnimator().apply {
                    setPropertyName("alpha")
                    setFloatValues(0f, 1f)
                },
                ObjectAnimator().apply {
                    setPropertyName("rotation")
                    setFloatValues(-90f, 0f)
                }
            )

            animators.add(this)
        }
    }

    private val fabHideAnimator by lazy {
        AnimatorSet().apply {
            duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(
                ObjectAnimator().apply {
                    setPropertyName("alpha")
                    setFloatValues(1f, 0f)
                },
                ObjectAnimator().apply {
                    setPropertyName("rotation")
                    setFloatValues(0f, -90f)
                }
            )

            animators.add(this)
        }
    }

    private val fadeInContentAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            setPropertyName("alpha")

            animators.add(this)
        }
    }

    private val fadeOutContentAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            setPropertyName("alpha")

            animators.add(this)
        }
    }

    private val timeSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = CycleInterpolator(1f)
            setPropertyName("alpha")
            setFloatValues(0f, 1f)

            animators.add(this)
        }
    }

    private val secondsSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = CycleInterpolator(1f)
            setPropertyName("alpha")
            setFloatValues(0f, 1f)

            animators.add(this)
        }
    }

    private val animators = mutableSetOf<Animator>()

    private fun Animator.reset(view: View) = apply {
        cancel()
        removeAllListeners() /*important. doOnStart(), doOnEnd() add! the listeners */
        setTarget(view)
    }

    fun stop() {
        for (animator in animators) {
            if (animator.isStarted) {
                animator.end()
            }
        }
    }

    fun showFab(view: View) {
        fabShowAnimator.apply {
            reset(view)
            doOnStart { view.visibility = VISIBLE }
            start()
        }
    }

    fun hideFab(view: View) {
        fabHideAnimator.apply {
            reset(view)
            doOnEnd { view.visibility = INVISIBLE }
            start()
        }
    }

    fun fadeInContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeInContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnStart(onStart)
            doOnEnd (onEnd)
            start()
        }
    }

    fun fadeOutContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onStart: (Animator) -> Unit = {},
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeOutContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnStart (onStart)
            doOnEnd (onEnd)
            start()
        }
    }

    fun blinkTimeSeparator(view: View) {
        timeSeparatorAnimator.apply {
            reset(view)
            start()
        }
    }

    fun blinkSecondsSeparator(view: View) {
        secondsSeparatorAnimator.apply {
            reset(view)
            start()
        }
    }

}