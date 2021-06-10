package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.SharedPreferencesWrapper

class Settings(context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun loadDefaults() = update {
       // clear()
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, true)
        putBooleanOptional(PREF_24_HOURS_FORMAT, true)
        putStringOptional(PREF_TIME_SEPARATOR, ":")
        putStringOptional(PREF_SECONDS_SEPARATOR, ":")
        putBooleanOptional(PREF_SECONDS_VISIBLE, true)
        putBooleanOptional(PREF_DATE_VISIBLE, true)
        putStringOptional(PREF_DATE_FORMAT, "yyyy-MM-dd")
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"

        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_24_HOURS_FORMAT = "24_hours_format"
        const val PREF_TIME_SEPARATOR = "time_separator"
        const val PREF_TIME_SEPARATOR_BLINKING = "time_separator_blinking"
        const val PREF_SECONDS_SEPARATOR = "seconds_separator"
        const val PREF_SECONDS_VISIBLE = "seconds_visible"
        const val PREF_DATE_VISIBLE = "date_visible"
        const val PREF_DATE_FORMAT = "date_format"

        const val PREF_HOURS_COLOR = "hours-color"
        const val PREF_BACKGROUND_COLOR = "background-color"
    }

}