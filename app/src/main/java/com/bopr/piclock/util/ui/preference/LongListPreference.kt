package com.bopr.piclock.util.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class LongListPreference : ListPreference {

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
        return persistLong(value.toLong())
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedLong(defaultReturnValue?.toLong() ?: 0).toString()
    }

    var numberValue: Long
        get() = value.toLong()
        set(v) {
            value = v.toString()
        }
}