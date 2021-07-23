package com.bopr.piclock.util.ui

import android.animation.AnimatorInflater.loadAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.AnimatorRes
import androidx.annotation.AttrRes
import androidx.annotation.Keep
import androidx.annotation.StyleRes
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.extendProperties
import com.bopr.piclock.util.property.PROP_RELATIVE_TRANSITION_X
import com.bopr.piclock.util.property.PROP_RELATIVE_TRANSITION_Y
import com.bopr.piclock.util.property.PROP_SCALE_Y_PIVOT_BOTTOM
import com.bopr.piclock.util.property.PROP_SCALE_Y_PIVOT_TOP
import com.bopr.piclock.util.resetRenderParams

/**
 * Text view with animated text transitions.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Suppress("JoinDeclarationAndAssignment")
@Keep
class AnimatedTextView : FrameLayout {

    private lateinit var view: ExtTextView
    private lateinit var shadowView: ExtTextView
    private var textAnimator: AnimatorSet? = null

    constructor(context: Context) : super(context)

    constructor(
        context: Context, attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        view = ExtTextView(context, attrs, defStyleAttr)
        shadowView = ExtTextView(context, attrs, defStyleAttr)

        resetViews()

        generateLayoutParams(attrs).apply {   /* do not use here layout's layoutParams */
            val w = if (width == WRAP_CONTENT) WRAP_CONTENT else MATCH_PARENT
            val h = if (height == WRAP_CONTENT) WRAP_CONTENT else MATCH_PARENT
            view.layoutParams = LayoutParams(w, h)
            shadowView.layoutParams = LayoutParams(w, h)
        }

        addView(shadowView)
        addView(view)

/*  DEBUG COLORS
        view.setBackgroundColor(Color.RED)
        shadowView.setBackgroundColor(Color.BLUE)
*/
    }

    override fun getBaseline(): Int {
        return view.baseline
    }

    private fun resetViews() {
        view.apply {
            resetRenderParams()
            visibility = VISIBLE
        }
        shadowView.apply {
            resetRenderParams()
            visibility = GONE
        }
    }

    fun setTextAnimator(@AnimatorRes resId: Int) {
        val animator = if (resId != 0) loadAnimator(context, resId) as AnimatorSet else null
        animator?.apply {
            if (childAnimations.size < 2) throw IllegalArgumentException("Invalid animation set")
        }

        textAnimator?.cancel()
        resetViews()
        textAnimator = animator?.apply {
            childAnimations[0].apply {
                extendProperties(CUSTOM_PROPERTIES)
                setTarget(view)
            }
            childAnimations[1].apply {
                extendProperties(CUSTOM_PROPERTIES)
                setTarget(shadowView)
            }
            doOnStart { shadowView.visibility = VISIBLE }
            doOnEnd { shadowView.visibility = GONE }
        }
    }

    fun getText(): CharSequence? {
        return view.text
    }

    fun setText(text: CharSequence?, animated: Boolean) {
        if (view.text != text) {
            textAnimator?.end()

            shadowView.text = view.text
            view.text = text
            view.requestLayout() /* important! */

            if (animated) textAnimator?.start()
        }
    }

    companion object {

        private val CUSTOM_PROPERTIES = setOf(
            PROP_RELATIVE_TRANSITION_X,
            PROP_RELATIVE_TRANSITION_Y,
            PROP_SCALE_Y_PIVOT_TOP,
            PROP_SCALE_Y_PIVOT_BOTTOM
        )
    }
}
