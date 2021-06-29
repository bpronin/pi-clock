package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.doOnLayoutComplete

class AnimatedTextView : FrameLayout {

    private lateinit var textView1: AppCompatTextView
    private lateinit var textView2: AppCompatTextView

    private lateinit var animator: Animator
    private lateinit var showAnimator: ObjectAnimator
    private lateinit var hideAnimator: ObjectAnimator

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        textView1 = AppCompatTextView(context, attrs, defStyleAttr)
        textView2 = AppCompatTextView(context, attrs, defStyleAttr)
        textView2.visibility = GONE

        addView(textView2)
        addView(textView1)

        showAnimator = ObjectAnimator.ofFloat(null, TRANSLATION_Y, 0f).apply {
            doOnStart { (target as View).visibility = VISIBLE }
        }
        hideAnimator = ObjectAnimator.ofFloat(null, TRANSLATION_Y, 0f).apply {
            doOnStart { (target as View).visibility = VISIBLE }
            doOnEnd { (target as View).visibility = GONE }
        }
        animator = AnimatorSet().apply {
            playTogether(
                showAnimator,
                hideAnimator
            )
            interpolator = BounceInterpolator()
            duration = 500
        }

        doOnLayoutComplete {
            val h = height * 0.8f
            showAnimator.setFloatValues(-h, 0f)
            hideAnimator.setFloatValues(0f, h)
        }
    }

    fun setText(text: CharSequence) {
        val front: AppCompatTextView
        val back: AppCompatTextView

        if (textView1.visibility == VISIBLE) {
            front = textView2
            back = textView1
        } else {
            front = textView1
            back = textView2
        }

        if (back.text != text) {
            front.text = text
            showAnimator.target = front
            hideAnimator.target = back
            animator.start()
        }
    }

}
