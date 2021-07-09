package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.forEachChild
import com.bopr.piclock.util.property.RELATIVE_TRANSITION_X_PROPERTY
import com.bopr.piclock.util.property.RELATIVE_TRANSITION_Y_PROPERTY

/**
 * Text view with animated transitions.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
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

        addView(shadowView, generateLayoutParams(attrs))
        addView(view, generateLayoutParams(attrs))

        resetViews()
    }

    private fun resetViews() {
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

/*  DEBUG COLORS
        view.setBackgroundColor(Color.RED)
        shadowView.setBackgroundColor(Color.BLUE)
*/
    }

    fun setTextAnimator(resId: Int) {
        val animator = if (resId > 0) loadAnimator(context, resId) as AnimatorSet else null
        animator?.apply {
            if (childAnimations.size < 2) throw IllegalArgumentException("Invalid animation set")
        }

        textAnimator?.cancel()
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
            doOnEnd { shadowView.visibility = GONE }
        }
    }

    fun setText(text: CharSequence, animated: Boolean) {
        if (view.text != text) {
            shadowView.text = view.text
            view.text = text
            if (animated) {
                textAnimator?.start()
            }
        }
    }

    private fun Animator.extendProperties() {
        forEachChild { child ->
            child.apply {
                if (this is ObjectAnimator) {
                    when (propertyName) {
                        RELATIVE_TRANSITION_X_PROPERTY.name -> setProperty(RELATIVE_TRANSITION_X_PROPERTY)
                        RELATIVE_TRANSITION_Y_PROPERTY.name -> setProperty(RELATIVE_TRANSITION_Y_PROPERTY)
                    }
                }
            }
        }
    }

}
