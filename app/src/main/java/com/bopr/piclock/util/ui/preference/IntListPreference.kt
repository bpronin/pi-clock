package com.bopr.piclock.util.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class IntListPreference : ListPreference {

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
        return persistInt(value.toInt())
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedInt(defaultReturnValue?.toInt() ?: 0).toString()
    }
}