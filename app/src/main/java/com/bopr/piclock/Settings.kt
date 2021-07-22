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
), Contextual {

    constructor(owner: Contextual) : this(owner.requireContext())

    override fun requireContext(): Context {
        return context
    }

    private fun EditorWrapper.putIntResourceOptional(key: String, value: Int, valuesRes: Int) {
        putIntOptional(
            key, ensureResArrayContains(valuesRes, value)
        ) {
            isResArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putLongResourceOptional(key: String, value: Long, valuesRes: Int) {
        putLongOptional(
            key, ensureResArrayContains(valuesRes, value)
        ) {
            isResArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putStringResourceOptional(
        key: String,
        value: String,
        valuesRes: Int
    ) {
        putStringOptional(
            key, ensureResArrayContains(valuesRes, value)
        ) {
            isResArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putStringSetResourceOptional(
        key: String,
        value: Set<String>,
        valuesRes: Int
    ) {
        putStringSetOptional(
            key, ensureAllResExists(valuesRes, value)
        ) {
            isResArrayContainsAll(valuesRes, it)
        }
    }

    fun validate() = update {
        /* NOTE: clearing settings here will have no effect. it should be done in separate update */
        putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
        putBooleanOptional(PREF_TIME_SEPARATORS_BLINKING, true)
        putBooleanOptional(PREF_ANIMATION_ON, true)
        putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
        putBooleanOptional(PREF_TIME_SEPARATORS_VISIBLE, true)
        putBooleanOptional(PREF_GESTURES_ENABLED, true)
        putBooleanOptional(PREF_SECOND_HAND_VISIBLE, true)
        putBooleanOptional(PREF_CLOCK_HAND_MOVE_SMOOTH, true)
        putIntOptional(PREF_WEEK_START, localeFirstDayOfWeek)
        putLongOptional(PREF_CONTENT_FLOAT_INTERVAL, 900000L)  /* 15 min */
        putIntOptional(PREF_CONTENT_SCALE, 100) {
            it in MIN_SCALE..MAX_SCALE
        }
        putIntOptional(PREF_MUTED_BRIGHTNESS, 20) {
            it in MIN_BRIGHTNESS..MAX_BRIGHTNESS
        }

        putStringSetResourceOptional(
            PREF_TICK_RULES,
            setOf(TICK_ACTIVE),
            R.array.tick_sound_mode_values
        )

        putStringResourceOptional(
            PREF_HOURS_MINUTES_FORMAT,
            if (localeIs24Hour) "HH:mm" else "h:mm",
            R.array.hours_minutes_format_values
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

        putIntResourceOptional(
            PREF_FLOAT_SPEED,
            100,
            R.array.content_float_speed_values
        )

        putStringOptional(
            PREF_CONTENT_LAYOUT,
            ensureResArrayContains(
                R.array.content_layout_values,
                getResName(R.layout.view_digital_default)
            ),
            isOldValueValid = { oldLayoutName ->
                if (isResArrayContains(R.array.content_layout_values, oldLayoutName)) {
                    getLayoutStyles(oldLayoutName).contains(getString(PREF_CONTENT_STYLE, null))
                } else
                    false
            },
            onPut = { newLayoutName ->
                getLayoutStyles(newLayoutName!!).apply {
                    putString(PREF_CONTENT_STYLE, get(0))
                }
            }
        )

        putStringResourceOptional(
            PREF_TICK_SOUND,
            getResName(R.raw.alarm_clock),
            R.array.tick_sound_values
        )

        putStringResourceOptional(
            PREF_DIGITS_ANIMATION,
            getResName(R.animator.text_fade_trough_linear),
            R.array.digits_animation_values
        )

        putStringResourceOptional(
            PREF_FLOAT_ANIMATION,
            getResName(R.animator.float_move),
            R.array.float_animation_values
        )

        putStringResourceOptional(
            PREF_CLOCK_HAND_ANIMATION,
            getResName(R.animator.clock_handle_rotate_overshot),
            R.array.clock_hand_animation_values
        )
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

        const val PREF_ANIMATION_ON = "animation_on"
        const val PREF_AUTO_INACTIVATE_DELAY = "auto_inactivate_delay"
        const val PREF_CLOCK_HAND_ANIMATION = "clock_hand_animation"
        const val PREF_CLOCK_HAND_MOVE_SMOOTH = "clock_hand_move_smooth"
        const val PREF_CONTENT_FLOAT_INTERVAL = "content_float_interval"
        const val PREF_CONTENT_LAYOUT = "content_layout"
        const val PREF_CONTENT_SCALE = "content_scale"
        const val PREF_CONTENT_STYLE = "content_style"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_DIGITS_ANIMATION = "digits_animation"
        const val PREF_FLOAT_ANIMATION = "float_animation"
        const val PREF_FLOAT_SPEED = "float_speed"
        const val PREF_FULLSCREEN_ENABLED = "fullscreen_enabled"
        const val PREF_GESTURES_ENABLED = "gestures_enabled"
        const val PREF_MUTED_BRIGHTNESS = "muted_brightness"
        const val PREF_SECONDS_FORMAT = "seconds_format"
        const val PREF_SECOND_HAND_VISIBLE = "second_hand_visible"
        const val PREF_TICK_RULES = "tick_sound_mode"
        const val PREF_TICK_SOUND = "tick_sound"
        const val PREF_HOURS_MINUTES_FORMAT = "hours_minutes_format"
        const val PREF_TIME_SEPARATORS_BLINKING = "time_separators_blinking"
        const val PREF_TIME_SEPARATORS_VISIBLE = "time_separators_visible"
        const val PREF_WEEK_START = "week_start"
    }

}