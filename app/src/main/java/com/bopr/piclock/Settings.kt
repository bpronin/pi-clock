package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.SharedPreferencesWrapper
import com.bopr.piclock.util.getResourceName
import com.bopr.piclock.util.getStringArray
import com.bopr.piclock.util.isResourceExists

class Settings(private val context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun validate() = update {
//        clear()
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, true)
        putBooleanOptional(PREF_24_HOURS_FORMAT, true)
        putBooleanOptional(PREF_SECONDS_VISIBLE, true)
        putBooleanOptional(PREF_DATE_VISIBLE, true)
        putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
//        putIntOptional(PREF_CLOCK_BRIGHTNESS, 30)
        putLongOptional(PREF_AUTO_FULLSCREEN_DELAY, 5000L)
        putStringOptional(PREF_DATE_FORMAT, context.getStringArray(R.array.date_format_values)[0])

        putStringOptional(
            PREF_CLOCK_LAYOUT,
            context.getResourceName(R.layout.view_digital_default)
        ) {
            getString(PREF_CLOCK_LAYOUT, null)?.let {
                context.isResourceExists("layout", it)
            } ?: false
        }

        putBooleanOptional(PREF_TICK_SOUND_ALWAYS, true)
        putStringOptional(PREF_TICK_SOUND, "") {
            val current = getString(PREF_TICK_SOUND)
            current.isEmpty() || context.isResourceExists("raw", current)
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
        const val PREF_TICK_SOUND_ALWAYS = "tick_sound_always"
        const val PREF_CLOCK_LAYOUT = "clock_layout"
        const val PREF_AUTO_FULLSCREEN_DELAY = "auto_fullscreen_delay"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
//        const val PREF_CLOCK_BRIGHTNESS = "clock_brightness"
    }

}