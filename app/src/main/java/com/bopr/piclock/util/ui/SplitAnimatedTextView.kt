package com.bopr.piclock.util.ui

import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.annotation.AnimatorRes
import androidx.annotation.AttrRes
import androidx.annotation.Keep
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import com.bopr.piclock.R

/**
 * Text view with animated text transitions. Every digit animated separately.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Keep
class SplitAnimatedTextView : LinearLayout {

    private lateinit var views: Array<AnimatedTextView>
    private lateinit var primaryView: AnimatedTextView
    private var digitCount = 2
    private var text: CharSequence? = null

    var splitDigits = true
        set(value) {
            if (field != value) {
                field = value
                views.forEach { view ->
                    if (view != primaryView) {
                        view.setText(null, false) /* this stops animation if any */
                        view.isVisible = field
                    }
                }
                updateViewsText(false)
            }
        }

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
        context.obtainStyledAttributes(attrs, R.styleable.SplitAnimatedTextView, defStyleAttr, 0)
            .apply {
                digitCount = getInt(R.styleable.SplitAnimatedTextView_digitsCount, digitCount)
                splitDigits = getBoolean(R.styleable.SplitAnimatedTextView_splitDigits, splitDigits)

                recycle()
            }

        views = Array(digitCount) { index ->
            AnimatedTextView(context, attrs, defStyleAttr).also { view ->
                view.layoutParams = if (index < digitCount - 1)
                    LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                else
                    LayoutParams(0, WRAP_CONTENT, 1f)
                view.gravity = Gravity.START
                addView(view)
            }
        }
        primaryView = views.last()

        orientation = HORIZONTAL
        setText(primaryView.getText(), false)

        /*  DEBUG COLOR:  primaryView.setBackgroundColor(Color.RED) */
    }

    override fun getBaseline(): Int {
        return primaryView.baseline
    }

    private fun updateViewsText(animated: Boolean) {
        if (splitDigits) {
            text?.apply {
                views.forEachIndexed { index, view ->
                    val s = if (index < length) get(index).toString() else null
                    view.setText(s, animated)
                }
            } ?: apply {
                views.forEach { view ->
                    view.setText(null, animated)
                }
            }
        } else {
            primaryView.setText(text, animated)
        }
    }

    fun setText(text: CharSequence?, animated: Boolean) {
        this.text = text
        updateViewsText(animated)
    }

    fun setTextAnimators(@AnimatorRes resId: Int) {
        views.forEach { view ->
            view.setTextAnimator(resId)
        }
    }

    fun setTextAnimators(vararg animator: AnimatorSet?) {
        views.forEachIndexed { index, view ->
            view.setTextAnimator(animator[index])
        }
    }

}