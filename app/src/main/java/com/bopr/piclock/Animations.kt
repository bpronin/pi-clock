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
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.getParentView
import com.bopr.piclock.util.getRect
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

    private val floatContentAnimator by lazy {
        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun Animator.reset(view: View) = apply {
        cancel()
        removeAllListeners() /*important. doOnStart(), doOnEnd() add! the listeners */
        setTarget(view)
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
            doOnEnd { view.visibility = GONE }
            start()
        }
    }

    fun showInfo(view: View) {
        showInfoAnimator.apply {
            reset(view)
            doOnStart { view.visibility = VISIBLE }
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

    fun fitScaleIntoParent(view: View, onEnd: () -> Unit) {
        val pr = view.getParentView().getScaledRect()
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
        val pr = view.getParentView().getScaledRect()
        val vr = view.getScaledRect()
        val dw = pr.width() - vr.width()
        val dh = pr.height() - vr.height()
        val dx = view.x - vr.left
        val dy = view.y - vr.top

        floatTo(
            view,
            random().toFloat() * dw + dx,
            random().toFloat() * dh + dy,
            1500L,
            onEnd
        )
    }

    fun floatContentHome(view: View, onEnd: (Animator) -> Unit = {}) {
        val pr = view.getParentView().getRect()
        val vr = view.getRect()

        floatTo(
            view,
            (pr.width() - vr.width()) / 2,
            (pr.height() - vr.height()) / 2,
            1000L,
            onEnd
        )
    }

    private fun floatTo(
        view: View,
        x: Float,
        y: Float,
        floatDuration: Long,
        onEnd: (Animator) -> Unit = {}
    ) {
        floatContentAnimator.apply {
            reset(view)
            playTogether(
                ObjectAnimator.ofFloat(view, X, view.x, x),
                ObjectAnimator.ofFloat(view, Y, view.y, y)
            )
            duration = floatDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnEnd(onEnd)
            start()
        }
    }

}

