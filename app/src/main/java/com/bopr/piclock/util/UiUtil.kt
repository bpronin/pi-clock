package com.bopr.piclock.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.core.content.ContextCompat
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

/**
 * Returns text underlined with wavy red line.
 */
fun Context.underwivedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: ParagraphStyle = WavyUnderlineSpan(ContextCompat.getColor(this, R.color.error_underline))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

/**
 * Returns text of accent color.
 */
fun Context.accentedText(value: CharSequence?): Spannable {
    val spannable: Spannable = SpannableString(value)
    val span: CharacterStyle =
        ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_accent))
    spannable.setSpan(span, 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return spannable
}

fun View.showAnimated(@AnimRes animationRes: Int, delay: Long) {
    if (visibility != View.VISIBLE) {
        clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, animationRes).apply {
            startOffset = delay

            setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                    visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    /* nothing */
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    /* nothing */
                }
            })
        }
        visibility = View.INVISIBLE /* to properly animate coordinates ensure it is not GONE here */
        startAnimation(animation)
    }
}

fun View.hideAnimated(@AnimRes animationRes: Int, delay: Long) {
    if (visibility == View.VISIBLE) {
        clearAnimation()
        val animation = AnimationUtils.loadAnimation(context, animationRes).apply {
            startOffset = delay

            setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {
                    visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    /* nothing */
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    /* nothing */
                }
            })
        }
        visibility = View.VISIBLE /* to properly animate coordinates ensure it is not GONE here */
        startAnimation(animation)
    }
}
