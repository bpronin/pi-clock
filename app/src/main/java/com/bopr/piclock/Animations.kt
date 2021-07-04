package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.getScaledRect
import com.bopr.piclock.util.parentView
import kotlin.math.min


internal class Animations {

//    private val fabShowAnimator by lazy {
//        AnimatorSet().apply {
//            playTogether(
//                ObjectAnimator().apply {
//                    setProperty(ALPHA)
//                    setFloatValues(0f, 1f)
//                },
//                ObjectAnimator().apply {
//                    setProperty(ROTATION)
//                    setFloatValues(-90f, 0f)
//                }
//            )
//            duration = 1000
//            interpolator = AccelerateInterpolator()
//        }
//    }

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

    private val infoAnimator by lazy {
        ObjectAnimator().apply {
            setProperty(ALPHA)
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
            duration = 700
            interpolator = DecelerateInterpolator()
        }
    }

    private fun Animator.reset(view: View) = apply {
        cancel()
        removeAllListeners() /*important. doOnStart(), doOnEnd() add! the listeners */
        setTarget(view)
    }

//    fun showFab(view: View) {
//        fabShowAnimator.apply {
//            reset(view)
//            doOnStart { view.visibility = VISIBLE }
//            start()
//        }
//    }
//
//    fun hideFab(view: View) {
//        fabHideAnimator.apply {
//            reset(view)
//            doOnEnd { view.visibility = GONE }
//            start()
//        }
//    }

    fun showInfo(view: View) {
        infoAnimator.apply {
            reset(view)
            setFloatValues(0f, 1f)
            duration = 500
            doOnStart { view.visibility = VISIBLE }
            start()
        }
    }

    fun hideInfo(view: View) {
        infoAnimator.apply {
            reset(view)
            setFloatValues(1f, 0f)
            duration = 1000
            doOnEnd { view.visibility = GONE }
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
        val pr = view.parentView.getScaledRect()
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

}

