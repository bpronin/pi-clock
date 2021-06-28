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
        /* NOTE: clearing settings here will have no effect */
        context.apply {
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putBooleanOptional(PREF_TIME_SEPARATORS_BLINKING, true)
            putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
            putBooleanOptional(PREF_TIME_SEPARATORS_VISIBLE, true)
            putLongOptional(PREF_CONTENT_FLOAT_INTERVAL, 900000L)
            putFloatOptional(PREF_CONTENT_SCALE, 1f)

            putStringSetOptional(
                PREF_TICK_SOUND_MODE,
                ensureAllResExists(R.array.tick_sound_mode_values, setOf(TICK_ACTIVE))
            ) {
                isAllResExists(R.array.tick_sound_mode_values, getStringSet(PREF_TICK_SOUND_MODE))
            }

            putStringOptional(
                PREF_TIME_FORMAT,
                ensureResExists(
                    R.array.time_format_values,
                    if (is24HourLocale()) "HH:mm" else "h:mm"
                )
            ) {
                isResExists(R.array.time_format_values, getString(PREF_TIME_FORMAT))
            }

            putStringOptional(
                PREF_SECONDS_FORMAT,
                ensureResExists(R.array.seconds_format_values, "ss")
            ) {
                isResExists(R.array.seconds_format_values, getString(PREF_SECONDS_FORMAT))
            }

            putStringOptional(
                PREF_DATE_FORMAT,
                ensureResExists(R.array.date_format_values, SYSTEM_DEFAULT)
            ) {
                isResExists(R.array.date_format_values, getString(PREF_DATE_FORMAT))
            }

            putLongOptional(
                PREF_AUTO_DEACTIVATION_DELAY,
                ensureResExists(R.array.deactivation_delay_values, 5000)
            ) {
                isResExists(
                    R.array.deactivation_delay_values,
                    getLong(PREF_AUTO_DEACTIVATION_DELAY)
                )
            }

            putIntOptional(
                PREF_INACTIVE_BRIGHTNESS, 20
            ) {
                getInt(PREF_INACTIVE_BRIGHTNESS) in 0..100
            }

            putStringOptional(
                PREF_CONTENT_LAYOUT,
                ensureResExists(
                    R.array.content_layout_values,
                    getResName(R.layout.view_digital_default)
                )
            ) {
                isResExists(R.array.content_layout_values, getString(PREF_CONTENT_LAYOUT))
            }

            putStringOptional(
                PREF_TICK_SOUND,
                ensureResExists(R.array.tick_sound_values, getResName(R.raw.alarm_clock))
            ) {
                isResExists(R.array.tick_sound_values, getString(PREF_TICK_SOUND))
            }
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 1

        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"
        const val SYSTEM_DEFAULT = "system_default"
        const val TICK_ACTIVE = "active"
        const val TICK_INACTIVE = "inactive"
        const val TICK_FLOATING = "floating"

        val DEFAULT_DATE_FORMAT: DateFormat = DateFormat.getDateInstance(FULL)

        const val PREF_SETTINGS_VERSION = "settings_version" /* internal */
        const val PREF_TOP_SETTING = "top_setting" /* internal */
        const val PREF_ABOUT = "about_app" /* internal, marker */

        const val PREF_TIME_FORMAT = "time_format"
        const val PREF_AUTO_DEACTIVATION_DELAY = "auto_deactivation_delay"
        const val PREF_CONTENT_FLOAT_INTERVAL = "content_float_interval"
        const val PREF_CONTENT_LAYOUT = "content_layout"
        const val PREF_CONTENT_SCALE = "content_scale"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
        const val PREF_INACTIVE_BRIGHTNESS = "min_brightness"
        const val PREF_SECONDS_FORMAT = "seconds_format"
        const val PREF_TICK_SOUND = "tick_sound"
        const val PREF_TICK_SOUND_MODE = "tick_sound_mode"
        const val PREF_TIME_SEPARATORS_BLINKING = "time_separators_blinking"
        const val PREF_TIME_SEPARATORS_VISIBLE = "time_separators_visible"
    }

}