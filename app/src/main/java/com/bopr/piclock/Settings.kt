package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.*

class Settings(private val context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun validate() = update {
//        clear()
        context.run {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, true)
            putBooleanOptional(PREF_24_HOURS_FORMAT, true)
            putBooleanOptional(PREF_SECONDS_VISIBLE, true)
            putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
            putBooleanOptional(PREF_TICK_SOUND_ALWAYS, true)

            putStringOptional(
                PREF_DATE_FORMAT,
                getResArrayValue(R.array.date_format_values, 0)
            ) {
                val current = getString(PREF_DATE_FORMAT).toString()
                isResArrayValueExists(R.array.date_format_values, current)
            }

            putLongOptional(
                PREF_AUTO_FULLSCREEN_DELAY,
                getResArrayValue(R.array.auto_fullscreen_delay_values, 0).toLong()
            ) {
                val current = getLong(PREF_AUTO_FULLSCREEN_DELAY).toString()
                isResArrayValueExists(R.array.auto_fullscreen_delay_values, current)
            }

            putIntOptional(
                PREF_CLOCK_BRIGHTNESS,
                getResArrayValue(R.array.clock_brightness_values, 0).toInt()
            ) {
                val current = getInt(PREF_CLOCK_BRIGHTNESS).toString()
                isResArrayValueExists(R.array.clock_brightness_values, current)
            }

            putStringOptional(
                PREF_CLOCK_LAYOUT,
                getResName(R.layout.view_digital_default)
            ) {
                getString(PREF_CLOCK_LAYOUT, null)?.let {
                    isResExists("layout", it)
                } ?: false
            }

            putStringOptional(PREF_TICK_SOUND, "") {
                val current = getString(PREF_TICK_SOUND)
                current.isEmpty() || isResExists("raw", current)
            }
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"

        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_24_HOURS_FORMAT = "24_hours_format"
        const val PREF_TIME_SEPARATOR_BLINKING = "time_separator_blinking"
        const val PREF_SECONDS_VISIBLE = "seconds_visible"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_TICK_SOUND = "tick_sound"
        const val PREF_TICK_SOUND_ALWAYS = "tick_sound_always"
        const val PREF_CLOCK_LAYOUT = "clock_layout"
        const val PREF_AUTO_FULLSCREEN_DELAY = "auto_fullscreen_delay"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
        const val PREF_CLOCK_BRIGHTNESS = "clock_brightness"
    }

}