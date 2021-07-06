package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.util.*
import java.text.DateFormat
import java.text.DateFormat.FULL

/**
 * Application settings.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
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
            putIntOptional(PREF_CONTENT_SCALE, 100) {
                getInt(PREF_CONTENT_SCALE) in MIN_SCALE..MAX_SCALE
            }
            putIntOptional(PREF_MUTED_BRIGHTNESS, 20) {
                getInt(PREF_MUTED_BRIGHTNESS) in MIN_BRIGHTNESS..MAX_BRIGHTNESS
            }

            putStringSetResourceOptional(
                PREF_TICK_RULES,
                setOf(TICK_ACTIVE),
                R.array.tick_sound_mode_values
            )

            putStringResourceOptional(
                PREF_TIME_FORMAT,
                if (is24HourLocale()) "HH:mm" else "h:mm",
                R.array.time_format_values
            )

            putStringResourceOptional(
                PREF_SECONDS_FORMAT,
                "ss",
                R.array.seconds_format_values
            )

            putStringResourceOptional(
                PREF_DATE_FORMAT,
                SYSTEM_DEFAULT,
                R.array.date_format_values
            )

            putLongResourceOptional(
                PREF_AUTO_INACTIVATE_DELAY,
                5000,
                R.array.auto_inactivate_delay_values
            )

            putStringResourceOptional(
                PREF_CONTENT_LAYOUT,
                getResName(R.layout.view_digital_default),
                R.array.content_layout_values
            )

            putStringResourceOptional(
                PREF_TICK_SOUND,
                getResName(R.raw.alarm_clock),
                R.array.tick_sound_values
            )

            putStringResourceOptional(
                PREF_DIGITS_ANIMATION,
                getResName(R.animator.text_slide_vertical_bounce),
                R.array.digits_animation_values
            )
        }
    }

    private fun EditorWrapper.putLongResourceOptional(key: String, value: Long, valuesRes: Int) {
        context.apply {
            putLongOptional(
                key,
                ensureResArrayContains(valuesRes, value)
            ) {
                isResArrayContains(valuesRes, getLong(key))
            }
        }
    }

    private fun EditorWrapper.putStringResourceOptional(
        key: String,
        value: String,
        valuesRes: Int
    ) {
        context.apply {
            putStringOptional(
                key,
                ensureResArrayContains(valuesRes, value)
            ) {
                isResArrayContains(valuesRes, getString(key))
            }
        }
    }

    private fun EditorWrapper.putStringSetResourceOptional(
        key: String,
        value: Set<String>,
        valuesRes: Int
    ) {
        context.apply {
            putStringSetOptional(
                key,
                ensureAllResExists(valuesRes, value)
            ) {
                isResArrayContainsAll(valuesRes, getStringSet(key))
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
        const val PREF_AUTO_INACTIVATE_DELAY = "auto_inactivate_delay"
        const val PREF_CONTENT_FLOAT_INTERVAL = "content_float_interval"
        const val PREF_CONTENT_LAYOUT = "content_layout"
        const val PREF_CONTENT_SCALE = "content_scale"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
        const val PREF_MUTED_BRIGHTNESS = "muted_brightness"
        const val PREF_SECONDS_FORMAT = "seconds_format"
        const val PREF_TICK_SOUND = "tick_sound"
        const val PREF_TICK_RULES = "tick_sound_mode"
        const val PREF_TIME_SEPARATORS_BLINKING = "time_separators_blinking"
        const val PREF_TIME_SEPARATORS_VISIBLE = "time_separators_visible"
        const val PREF_DIGITS_ANIMATION = "digits_animation"
    }

}