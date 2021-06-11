package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.SharedPreferencesWrapper

class Settings(private val context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun validate() = update {
        // clear()
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, true)
        putBooleanOptional(PREF_24_HOURS_FORMAT, true)
        putBooleanOptional(PREF_SECONDS_VISIBLE, true)
        putBooleanOptional(PREF_DATE_VISIBLE, true)
        putStringOptional(PREF_DATE_FORMAT, "yyyy-MM-dd")
        putStringOptional(PREF_TICK_SOUND, "") {
            val current = getString(PREF_TICK_SOUND)
            current.isEmpty() || context.resources.getIdentifier(current, "raw", context.packageName) != 0
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"

        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_24_HOURS_FORMAT = "24_hours_format"
        const val PREF_TIME_SEPARATOR_BLINKING = "time_separator_blinking"
        const val PREF_SECONDS_VISIBLE = "seconds_visible"
        const val PREF_DATE_VISIBLE = "date_visible"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_TICK_SOUND = "tick_sound"
    }

}