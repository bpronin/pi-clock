package com.bopr.piclock.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun <T : View?> View.requireViewByIdCompat(@IdRes id: Int): T {
    return findViewById(id)
        ?: throw IllegalArgumentException("ID does not reference a View inside this View")
}

/**
 * Returns text underlined with wavy red line.
 */
fun Context.underwivedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: ParagraphStyle =
        WavyUnderlineSpan(ContextCompat.getColor(this, R.color.error_underline))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

/**
 * Returns text of accent color.
 */
fun Context.accentedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: CharacterStyle =
        ForegroundColorSpan(ContextCompat.getColor(this, R.color.bordo))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
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