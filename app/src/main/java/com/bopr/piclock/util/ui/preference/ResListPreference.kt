package com.bopr.piclock.util.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class ResListPreference : ListPreference {

    @Suppress("unused")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        convertEntryValues()
    }

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        convertEntryValues()
    }

    @Suppress("unused")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        convertEntryValues()
    }

    @Suppress("unused")
    constructor(context: Context?) : super(context)

    override fun setEntryValues(entryValuesResId: Int) {
        super.setEntryValues(entryValuesResId)
        convertEntryValues()
    }

    /**
     * Transforms resource paths to names
     */
    private fun convertEntryValues() {
        entryValues = entryValues.map { resPath ->
            (resPath as String).substringAfter("/").substringBeforeLast(".")
        }.toTypedArray()
    }

}