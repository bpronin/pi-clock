package com.bopr.piclock.util.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import com.bopr.piclock.R

/**
 * Workaround to fix [AppCompatTextView]'s issue with ignored negative vertical paddings.
 */
@Keep
class ExtTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(
        context: Context, attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        context.obtainStyledAttributes(attrs, R.styleable.ExtTextView, defStyleAttr, 0).apply {
            setPadding(
                paddingLeft,
                getDimensionPixelSize(R.styleable.ExtTextView_android_paddingTop, 0),
                paddingRight,
                getDimensionPixelSize(R.styleable.ExtTextView_android_paddingBottom, 0)
            )
            recycle()
        }
    }

}