package com.bopr.piclock.util

import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import androidx.annotation.AnimRes
import androidx.annotation.IdRes

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun <T : View?> View.requireViewByIdCompat(@IdRes id: Int): T {
    return findViewById(id)
        ?: throw IllegalArgumentException("ID does not reference a View inside this View")
}

fun View.animateRes(
    @AnimRes animationRes: Int,
    startDelay: Long?,
    duration: Long?,
    onStart: () -> Unit = {},
    onEnd: () -> Unit = {}
) {
    clearAnimation()
    val animation = loadAnimation(context, animationRes).also {
        duration?.apply { it.duration = this }
        startDelay?.apply { it.startOffset = this }

        it.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                onStart()
            }

            override fun onAnimationEnd(animation: Animation?) {
                onEnd()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                /* do nothing */
            }
        })
    }

    startAnimation(animation)
}

fun View.showAnimated(@AnimRes animationRes: Int, startDelay: Long) {
    if (visibility != VISIBLE) {
        animateRes(animationRes, startDelay, null, onStart = { visibility = VISIBLE })
    }
}

fun View.hideAnimated(@AnimRes animationRes: Int, startDelay: Long) {
    if (visibility == VISIBLE) {
        animateRes(animationRes, startDelay, null, onStart = { visibility = INVISIBLE })
    }
}