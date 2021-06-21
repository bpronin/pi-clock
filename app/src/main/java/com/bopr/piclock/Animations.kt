package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getScaledRect
import java.lang.Math.random
import kotlin.math.min

internal class Animations {

    private val fabShowAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator().apply {
                    setProperty(ALPHA)
                    setFloatValues(0f, 1f)
                },
                ObjectAnimator().apply {
                    setProperty(ROTATION)
                    setFloatValues(-90f, 0f)
                }
            )
            duration = 1000
            interpolator = AccelerateInterpolator()
        }
    }

    private val fabHideAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator().apply {
                    setProperty(ALPHA)
                    setFloatValues(1f, 0f)
                },
                ObjectAnimator().apply {
                    setProperty(ROTATION)
                    setFloatValues(0f, -90f)
                }
            )
            duration = 1000
            interpolator = AccelerateInterpolator()
        }
    }

    private val fadeInContentAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            duration = 2000
            interpolator = AccelerateInterpolator()
        }
    }

    private val fadeOutContentAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            duration = 2000
            interpolator = AccelerateInterpolator()
        }
    }

    private val showInfoAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            setFloatValues(0f, 1f)
            duration = 500
            interpolator = AccelerateInterpolator()
        }
    }

    private val hideInfoAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            setFloatValues(1f, 0f)
            duration = 500
            interpolator = AccelerateInterpolator()
        }
    }

    private val timeSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            setFloatValues(0f, 1f)
            duration = 2000
            interpolator = CycleInterpolator(1f)
        }
    }

    private val secondsSeparatorAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
            setFloatValues(0f, 1f)
            duration = 2000
            interpolator = CycleInterpolator(1f)
        }
    }

    private val fitScaleAnimator by lazy {
        AnimatorSet().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }
    }

    private var floatContentAnimator: Animator? = null

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

    fun showInfo(view: View) {
        view.visibility = VISIBLE
        showInfoAnimator.apply {
            reset(view)
            start()
        }
    }

    fun hideInfo(view: View) {
        hideInfoAnimator.apply {
            reset(view)
            doOnEnd { view.visibility = GONE }
            start()
        }
    }

    fun fadeInContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onEnd: () -> Unit = {}
    ) {
        fadeInContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnEnd { onEnd() }
            start()
        }
    }

    fun fadeOutContent(
        view: View,
        fromBrightness: Int,
        toBrightness: Int,
        onEnd: () -> Unit = {}
    ) {
        fadeOutContentAnimator.apply {
            reset(view)
            setFloatValues(fromBrightness / 100f, toBrightness / 100f)
            doOnEnd { onEnd() }
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

    fun fitToParent(view: View, onEnd: () -> Unit) {
        val pr = (view.parent as View).getScaledRect()
        val scale = min(pr.width() / view.width, pr.height() / view.height)

        fitScaleAnimator.apply {
            reset(view)
            playTogether(
                ObjectAnimator.ofFloat(view, SCALE_X, scale),
                ObjectAnimator.ofFloat(view, SCALE_Y, scale)
            )
            doOnEnd { onEnd() }
            start()
        }
    }

    fun floatContentSomewhere(view: View, onEnd: (Animator) -> Unit = {}) {
        val pr = (view.parent as View).getScaledRect()
        val vr = view.getScaledRect()

        val w = pr.width() - vr.width()
        val h = pr.height() - vr.height()

        floatTo(view, random().toFloat() * w, random().toFloat() * h, 15000, onEnd)
    }

    fun floatContentHome(view: View, onEnd: (Animator) -> Unit = {}) {
        val parent = view.parent as View

        val pr = parent.getScaledRect()
        val vr = view.getScaledRect()

        val w = pr.width() / 2 - vr.width() / 2
        val h = pr.height() / 2 - vr.height() / 2

        floatTo(view, w, h, 1000, onEnd)
    }

    private fun floatTo(
        view: View,
        x: Float,
        y: Float,
        floatDuration: Long,
        onEnd: (Animator) -> Unit = {}
    ) {
        floatContentAnimator?.cancel()
        floatContentAnimator = AnimatorSet().apply {
            duration = floatDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd(onEnd)

            val r = view.getScaledRect()
            playTogether(
                ObjectAnimator.ofFloat(view, X, x - r.left),
                ObjectAnimator.ofFloat(view, Y, y - r.top)
            )

            start()
        }
    }

}

