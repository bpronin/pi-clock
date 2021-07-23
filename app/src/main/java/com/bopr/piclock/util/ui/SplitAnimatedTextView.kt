package com.bopr.piclock.util.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.Keep
import androidx.annotation.StyleRes
import androidx.core.view.isGone
import com.bopr.piclock.R

/**
 * Text view with animated text transitions. Every digit animated separately.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Keep
class SplitAnimatedTextView : LinearLayout {

    private lateinit var digitViews: Array<AnimatedTextView>
    private var digitCount = 2
    private var text: CharSequence? = null

    var splitDigits = true
        set(value) {
            if (field != value) {
                field = value
                for (i in 0..digitViews.size - 2) {
                    digitViews[i].isGone = !splitDigits
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

        digitViews = Array(digitCount) { i ->
            val params = if (i < digitCount - 1)
                LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            else
                LayoutParams(0, WRAP_CONTENT, 1f)
            val view = AnimatedTextView(context, attrs, defStyleAttr)
            addView(view, params)
            view
        }

        orientation = HORIZONTAL
        setText(digitViews.last().getText(), false)
    }

    override fun getBaseline(): Int {
        return digitViews.last().baseline
    }

    private fun updateViewsText(animated: Boolean) {
        if (splitDigits) {
            digitViews.forEachIndexed { i, view ->
                val digitText = text?.run {
                    if (i < length) get(i).toString() else null
                }
                view.setText(digitText, animated)
            }
        } else {
            digitViews.last().setText(text, animated)
        }
    }

    fun setText(text: CharSequence?, animated: Boolean) {
        this.text = text
        updateViewsText(animated)
    }

    fun setTextAnimator(resId: Int) {
        digitViews.forEach { view ->
            view.setTextAnimator(resId)
        }
    }

}