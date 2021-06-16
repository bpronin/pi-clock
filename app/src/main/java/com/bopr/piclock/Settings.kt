package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.util.*
import java.text.DateFormat
import java.text.DateFormat.FULL

class Settings(private val context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
) {

    fun validate() = update {
        /* do not clear settings here  */
        context.apply {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putBooleanOptional(PREF_TIME_SEPARATOR_BLINKING, true)
            putBooleanOptional(PREF_24_HOURS_FORMAT, is24HourLocale())
            putBooleanOptional(PREF_SECONDS_VISIBLE, true)
            putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
            putBooleanOptional(PREF_TICK_SOUND_ALWAYS, false)
            putFloatOptional(PREF_CLOCK_SCALE, 1f)

            putStringOptional(
                PREF_DATE_FORMAT,
                ensureResExists(R.array.date_format_values, SYSTEM_DEFAULT)
            ) {
                isResExists(
                    R.array.date_format_values,
                    getString(PREF_DATE_FORMAT)
                )
            }

            putLongOptional(
                PREF_AUTO_FULLSCREEN_DELAY,
                ensureResExists(R.array.auto_fullscreen_delay_values, 5000)
            ) {
                isResExists(
                    R.array.auto_fullscreen_delay_values,
                    getLong(PREF_AUTO_FULLSCREEN_DELAY)
                )
            }

            putIntOptional(
                PREF_MIN_BRIGHTNESS,
                ensureResExists(R.array.min_brightness_values, 20)
            ) {
                isResExists(
                    R.array.min_brightness_values,
                    getInt(PREF_MIN_BRIGHTNESS)
                )
            }

            putStringOptional(
                PREF_CLOCK_LAYOUT,
                ensureResExists(
                    R.array.clock_layout_values,
                    getResName(R.layout.view_digital_default)
                )
            ) {
                isResExists(
                    R.array.clock_layout_values,
                    getString(PREF_CLOCK_LAYOUT)
                )
            }

            putStringOptional(
                PREF_TICK_SOUND,
                ensureResExists(
                    R.array.tick_sound_values,
                    getResName(R.raw.alarm_clock)
                )
            ) {
                isResExists(
                    R.array.tick_sound_values,
                    getString(PREF_TICK_SOUND)
                )
            }
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"

        const val SYSTEM_DEFAULT = "system_default"

        val DEFAULT_DATE_FORMAT: DateFormat = DateFormat.getDateInstance(FULL)

        const val PREF_24_HOURS_FORMAT = "24_hours_format"
        const val PREF_AUTO_FULLSCREEN_DELAY = "auto_fullscreen_delay"
        const val PREF_CLOCK_LAYOUT = "clock_layout"
        const val PREF_CLOCK_SCALE = "clock_scale"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
        const val PREF_MIN_BRIGHTNESS = "min_brightness"
        const val PREF_SECONDS_VISIBLE = "seconds_visible"
        const val PREF_SETTINGS_VERSION = "settings_version" /* hidden */
        const val PREF_TICK_SOUND = "tick_sound"
        const val PREF_TICK_SOUND_ALWAYS = "tick_sound_always"
        const val PREF_TIME_SEPARATOR_BLINKING = "time_separator_blinking"
    }

}