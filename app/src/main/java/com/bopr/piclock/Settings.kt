package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.SharedPreferencesWrapper

class Settings(context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun loadDefaults() = update {
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, DEFAULT_MINUTES_SEPARATOR_BLINKING)
        putBooleanOptional(PREF_24_HOURS_FORMAT, DEFAULT_24_HOURS_FORMAT)
        putStringOptional(PREF_TIME_SEPARATOR, DEFAULT_MINUTES_SEPARATOR)
        putStringOptional(PREF_SECONDS_SEPARATOR, DEFAULT_SECONDS_SEPARATOR)
        putBooleanOptional(PREF_SECONDS_VISIBLE, DEFAULT_SECONDS_VISIBLE)
        putLongOptional(PREF_AUTO_FULLSCREEN_DELAY, DEFAULT_AUTO_FULLSCREEN_DELAY)
        putStringOptional(PREF_DATE_FORMAT, "yyyy-MM-dd")
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"
        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */

        const val PREF_24_HOURS_FORMAT = "24_hours_format"
        const val PREF_AUTO_FULLSCREEN_DELAY = "auto_fullscreen_delay"
        const val PREF_TIME_SEPARATOR = "time_separator"
        const val PREF_TIME_SEPARATOR_BLINKING = "time_separator_blinking"
        const val PREF_SECONDS_SEPARATOR = "seconds_separator"
        const val PREF_SECONDS_VISIBLE = "seconds_visible"
        const val PREF_DATE_FORMAT = "date_format"

        const val PREF_HOURS_COLOR = "hours-color"
        const val PREF_BACKGROUND_COLOR = "background-color"

        const val DEFAULT = "default"
        const val DEFAULT_AUTO_FULLSCREEN_DELAY = 3000L
        const val DEFAULT_MINUTES_SEPARATOR = ":"
        const val DEFAULT_SECONDS_SEPARATOR = ":"
        const val DEFAULT_MINUTES_SEPARATOR_BLINKING = true
        const val DEFAULT_SECONDS_VISIBLE = true
        const val DEFAULT_24_HOURS_FORMAT = true
    }

}