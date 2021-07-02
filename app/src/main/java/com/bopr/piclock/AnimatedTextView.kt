package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.RELATIVE_TRANSLATION_X
import com.bopr.piclock.util.RELATIVE_TRANSLATION_Y

/**
 * Text view with animated transitions when changing text.
 */
class AnimatedTextView : FrameLayout {

    @Suppress("JoinDeclarationAndAssignment")
    private lateinit var view: AppCompatTextView
    private lateinit var shadowView: AppCompatTextView
    private var textAnimator: AnimatorSet? = null

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
        view = AppCompatTextView(context, attrs, defStyleAttr)
        shadowView = AppCompatTextView(context, attrs, defStyleAttr)

        addView(shadowView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER))
        addView(view, LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER))
        resetViews()
    }

    private fun resetViews() {
        // todo: should not the animators be responsible for it ?
        view.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            translationY = 0f
            translationX = 0f
            visibility = VISIBLE
        }
        shadowView.apply {
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            translationY = 0f
            translationX = 0f
            visibility = GONE
        }

//        view.setBackgroundColor(Color.RED)
//        shadowView.setBackgroundColor(Color.BLUE)
    }

    fun setTextAnimator(animator: AnimatorSet?) {
        animator?.apply {
            if (childAnimations.size != 2) throw IllegalArgumentException("Invalid animation set")
        }

        resetViews()

        textAnimator = animator?.apply {
            childAnimations[0].apply {
                setTarget(view)
                extendProperties()
            }
            childAnimations[1].apply {
                setTarget(shadowView)
                extendProperties()
            }
            doOnStart { shadowView.visibility = VISIBLE }
            doOnEnd {
                shadowView.visibility = GONE
                requestLayout()
            }
        }
    }

    fun setTextAnimatorRes(resId: Int) {
        setTextAnimator(if (resId > 0) loadAnimator(context, resId) as AnimatorSet else null)
    }

    fun setTextAnimated(text: CharSequence) {
        if (view.text != text) {
            shadowView.text = view.text
            view.text = text
            textAnimator?.start()
        }
    }

    private fun Animator.extendProperties() {
        if (this is ObjectAnimator) {
            when (propertyName) {
                RELATIVE_TRANSLATION_Y.name -> setProperty(RELATIVE_TRANSLATION_Y)
                RELATIVE_TRANSLATION_X.name -> setProperty(RELATIVE_TRANSLATION_X)
            }
        }
    }

}
