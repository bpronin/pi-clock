package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getScaledRect
import java.lang.Math.random

internal class ClockFragmentAnimations {

    private val fabShowAnimator by lazy {
        AnimatorSet().apply {
            duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(
                ObjectAnimator().apply {
                    setProperty(View.ALPHA)
                    setFloatValues(0f, 1f)
                },
                ObjectAnimator().apply {
                    setProperty(View.ROTATION)
                    setFloatValues(-90f, 0f)
                }
            )
        }
    }

    private val fabHideAnimator by lazy {
        AnimatorSet().apply {
            duration = 1000
            interpolator = AccelerateInterpolator()
            playTogether(
                ObjectAnimator().apply {
                    setProperty(View.ALPHA)
                    setFloatValues(1f, 0f)
                },
                ObjectAnimator().apply {
                    setProperty(View.ROTATION)
                    setFloatValues(0f, -90f)
                }
            )
        }
    }

    private val fadeInContentAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            setProperty(View.ALPHA)
        }
    }

    private val fadeOutContentAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = AccelerateInterpolator()
            setProperty(View.ALPHA)
        }
    }

    private val timeSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = CycleInterpolator(1f)
            setProperty(View.ALPHA)
            setFloatValues(0f, 1f)
        }
    }

    private val secondsSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            duration = 2000
            interpolator = CycleInterpolator(1f)
            setProperty(View.ALPHA)
            setFloatValues(0f, 1f)
        }
    }

    private fun Animator.reset(view: View) = apply {
        cancel()
        removeAllListeners() /*important. doOnStart(), doOnEnd() add! the listeners */
        setTarget(view)
    }

    fun showFab(view: View) {
        view.visibility = VISIBLE
        fabShowAnimator.apply {
            reset(view)
            start()
        }
    }

    fun hideFab(view: View) {
        fabHideAnimator.apply {
            reset(view)
            doOnEnd { view.visibility = GONE }
            start()
        }
    }

    fun fadeInContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeInContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnEnd(onEnd)
            start()
        }
    }

    fun fadeOutContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onEnd: (Animator) -> Unit = {}
    ) {
        fadeOutContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnEnd(onEnd)
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

    fun floatContentSomewhere(parent: View, view: View, onEnd: (Animator) -> Unit = {}) {
        val pr = parent.getScaledRect()
        val vr = view.getScaledRect()

        val w = pr.width() - vr.width()
        val h = pr.height() - vr.height()

        floatContentTo(
            view,
            random().toFloat() * w,
            random().toFloat() * h,
            onEnd
        )
    }

    fun floatContentTo(view: View, x: Float, y: Float, onEnd: (Animator) -> Unit = {}) {
        AnimatorSet().apply {
            duration = 5000
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd(onEnd)

            val r = view.getScaledRect()
            playTogether(
                ObjectAnimator.ofFloat(view, View.X, x - r.left),
                ObjectAnimator.ofFloat(view, View.Y, y - r.top)
            )

            start()
        }
    }

}

