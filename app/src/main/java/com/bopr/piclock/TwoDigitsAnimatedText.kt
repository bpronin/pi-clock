package com.bopr.piclock

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

class TwoDigitsAnimatedText : LinearLayout {

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
        orientation = HORIZONTAL

        hiDigitView = AnimatedTextView(context, attrs, defStyleAttr)
            .apply {
                setBackgroundColor(Color.RED)
            }
        loDigitView = AnimatedTextView(context, attrs, defStyleAttr)

        val lp = generateLayoutParams(attrs)
        val hiLp = LayoutParams(WRAP_CONTENT, lp.height, 0f)
        val loLp = LayoutParams(0, lp.height, 1f)

        addView(hiDigitView, hiLp)
        addView(loDigitView, loLp)

        setText(hiDigitView.getText(), false)
    }

    private lateinit var hiDigitView: AnimatedTextView
    private lateinit var loDigitView: AnimatedTextView

    fun setText(text: CharSequence?, animated: Boolean) {
        var hiText: CharSequence? = null
        var loText: CharSequence? = null
        text?.apply {
            hiText = get(0).toString()
            if (length > 1) loText = get(1).toString()
        }
        hiDigitView.setText(hiText, animated)
        loDigitView.setText(loText, animated)
    }

    fun setTextAnimator(hiAnimatorResId: Int, loAnimatorResId: Int = hiAnimatorResId) {
        hiDigitView.setTextAnimator(hiAnimatorResId)
        loDigitView.setTextAnimator(loAnimatorResId)
    }

}