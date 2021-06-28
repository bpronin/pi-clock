package com.bopr.piclock.util

import com.bopr.piclock.Settings
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class FloatSettingsPropertyDelegate(
    private val preferenceName: String,
    private val getSettings: () -> Settings
) : ReadWriteProperty<Any?, Float> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return getSettings().getFloat(preferenceName)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        getSettings().update { putFloat(preferenceName, value) }
    }
}
