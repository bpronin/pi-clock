package com.bopr.piclock.util.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class FloatListPreference : ListPreference {

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    @Suppress("unused")
    constructor(context: Context?) : super(context)

    override fun persistString(value: String): Boolean {
        return persistFloat(value.toFloat())
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedFloat(defaultReturnValue?.toFloat() ?: 0f).toString()
    }
}