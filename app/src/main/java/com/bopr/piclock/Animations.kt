package com.bopr.piclock

import android.animation.ObjectAnimator
import android.view.View
import android.view.View.*
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart


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

//    private val fabHideAnimator by lazy {
//        AnimatorSet().apply {
//            playTogether(
//                ObjectAnimator().apply {
//                    setProperty(ALPHA)
//                    setFloatValues(1f, 0f)
//                },
//                ObjectAnimator().apply {
//                    setProperty(ROTATION)
//                    setFloatValues(0f, -90f)
//                }
//            )
//            duration = 1000
//            interpolator = AccelerateInterpolator()
//        }
//    }


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

fun View.fadeInShow(fadeDuration: Long = 500L) {
    ObjectAnimator().apply {
        setProperty(ALPHA)
        setFloatValues(0f, 1f)
        setAutoCancel(true)
        interpolator = AccelerateInterpolator()
        duration = fadeDuration
        target = this@fadeInShow
        doOnStart { visibility = VISIBLE }

        start()
    }
}

fun View.fadeOutHide(fadeDuration: Long = 500L) {
    ObjectAnimator().apply {
        setProperty(ALPHA)
        setFloatValues(1f, 0f)
        setAutoCancel(true)
        interpolator = AccelerateInterpolator()
        duration = fadeDuration
        target = this@fadeOutHide
        doOnStart { visibility = VISIBLE }
        doOnEnd { visibility = GONE }

        start()
    }
}



