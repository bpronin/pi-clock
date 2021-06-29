package com.bopr.piclock

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView

class AnimatedTextView : FrameLayout {

    private lateinit var frontView: AppCompatTextView
    private lateinit var backView: AppCompatTextView

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
        layoutTransition = LayoutTransition()

        frontView = AppCompatTextView(context, attrs, defStyleAttr)
        backView = AppCompatTextView(context, attrs, defStyleAttr)
        backView.visibility = GONE

        addView(backView)
        addView(frontView)
    }
}