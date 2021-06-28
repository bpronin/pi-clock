package com.bopr.piclock.util

import com.bopr.piclock.Settings
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class IntSettingsPropertyDelegate(
    private val preferenceName: String,
    private val getSettings: () -> Settings
) : ReadWriteProperty<Any?, Int> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return getSettings().getInt(preferenceName)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        getSettings().update { putInt(preferenceName, value) }
    }
}
