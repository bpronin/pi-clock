package com.bopr.piclock

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.SoundControl.Companion.TICK_ACTIVE
import com.bopr.piclock.util.*

/**
 * Application settings.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Settings(private val context: Context) : SharedPreferencesWrapper(
    context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
), Contextual {

    val contentLayoutStyleName: String
        get() = getLayoutStyleName(
            getString(PREF_CONTENT_LAYOUT),
            getString(PREF_CONTENT_STYLE),
            getString(PREF_CONTENT_COLORS)
        )

    constructor(owner: Contextual) : this(owner.requireContext())

    override fun requireContext(): Context {
        return context
    }

    private fun EditorWrapper.putIntResOptional(key: String, value: Int, valuesRes: Int) {
        putIntOptional(
            key, ensureStringArrayContains(valuesRes, value)
        ) {
            isStringArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putLongResOptional(key: String, value: Long, valuesRes: Int) {
        putLongOptional(
            key, ensureStringArrayContains(valuesRes, value)
        ) {
            isStringArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putStringResOptional(
        key: String,
        value: String,
        valuesRes: Int
    ) {
        putStringOptional(
            key, ensureStringArrayContains(valuesRes, value)
        ) {
            isStringArrayContains(valuesRes, it)
        }
    }

    private fun EditorWrapper.putTypedResOptional(
        key: String,
        valueRes: Int,
        valuesRes: Int,
        isOldValueValid: (oldValue: String) -> Boolean = { true }
    ) {
        putStringOptional(
            key, ensureRefArrayContains(valuesRes, getResName(valueRes))
        ) {
            isRefArrayContains(valuesRes, it) && isResExists(it) && isOldValueValid(it)
        }
    }

    private fun EditorWrapper.putStringSetResOptional(
        key: String,
        value: Set<String>,
        valuesRes: Int
    ) {
        putStringSetOptional(
            key, ensureStringArrayContainsAll(valuesRes, value)
        ) {
            isStringArrayContainsAll(valuesRes, it)
        }
    }

    fun validate() {
        update {
            /* NOTE: clearing settings here will have no effect. it should be done in separate update */
            putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION)
            putBooleanOptional(PREF_TIME_SEPARATORS_BLINKING, true)
            putBooleanOptional(PREF_ANIMATION_ON, true)
            putBooleanOptional(PREF_FULLSCREEN_ENABLED, true)
            putBooleanOptional(PREF_TIME_SEPARATORS_VISIBLE, true)
            putBooleanOptional(PREF_GESTURES_ENABLED, true)
            putBooleanOptional(PREF_SECOND_HAND_VISIBLE, true)
            putBooleanOptional(PREF_CLOCK_HAND_MOVE_SMOOTH, true)
            putBooleanOptional(PREF_DIGITS_SPLIT_ANIMATION, true)
            putIntOptional(PREF_WEEK_START, localeFirstDayOfWeek)
            putIntOptional(PREF_CONTENT_SCALE, 100) { it in MIN_SCALE..MAX_SCALE }
            putIntOptional(PREF_MUTED_BRIGHTNESS, 30) { it in MIN_BRIGHTNESS..MAX_BRIGHTNESS }

            putTypedResOptional(
                PREF_CONTENT_LAYOUT,
                DEFAULT_LAYOUT,
                R.array.content_layout_values
            ) {
                isResExists(contentLayoutStyleName)
            }

            val layoutName = getResName(DEFAULT_LAYOUT)

            val stylesResId = requireStyleValuesResId(layoutName)
            putStringOptional(
                PREF_CONTENT_STYLE,
                requireStringArray(stylesResId)[0]
            ) {
                isStringArrayContains(stylesResId, it) && isResExists(contentLayoutStyleName)
            }

            val colorsResId = getColorsValuesResId(layoutName)
            putStringOptional(
                PREF_CONTENT_COLORS,
                requireStringArray(colorsResId)[3]
            ) {
                isStringArrayContains(colorsResId, it) && isResExists(contentLayoutStyleName)
            }

            putStringSetResOptional(
                PREF_TICK_RULES,
                setOf(TICK_ACTIVE),
                R.array.tick_sound_mode_values
            )

            putStringResOptional(
                PREF_HOURS_MINUTES_FORMAT,
                requireStringArray(R.array.hours_minutes_format_values)[if (localeIs24Hour) 0 else 2],
                R.array.hours_minutes_format_values
            )

            putStringResOptional(
                PREF_SECONDS_FORMAT,
                requireStringArray(R.array.seconds_format_values)[0],
                R.array.seconds_format_values
            )

            putStringResOptional(
                PREF_DATE_FORMAT,
                SYSTEM_DEFAULT,
                R.array.date_format_values
            )

            putLongResOptional(
                PREF_AUTO_INACTIVATE_DELAY,
                requireStringArray(R.array.auto_inactivate_delay_values)[1].toLong(),
                R.array.auto_inactivate_delay_values
            )

            putLongResOptional(
                PREF_CONTENT_FLOAT_INTERVAL,
                requireStringArray(R.array.content_float_interval_values)[3].toLong(),
                R.array.content_float_interval_values
            )

            putIntResOptional(
                PREF_FLOAT_SPEED,
                requireStringArray(R.array.content_float_speed_values)[3].toInt(),
                R.array.content_float_speed_values
            )

            putTypedResOptional(
                PREF_TICK_SOUND,
                R.raw.alarm_clock,
                R.array.tick_sound_values
            )

            putTypedResOptional(
                PREF_DIGITS_ANIMATION,
                R.animator.text_fall_accelerate,
                R.array.digits_animation_values
            )

            putTypedResOptional(
                PREF_FLOAT_ANIMATION,
                R.animator.float_move,
                R.array.float_animation_values
            )

            putTypedResOptional(
                PREF_CLOCK_HAND_ANIMATION,
                R.animator.clock_handle_rotate_overshot,
                R.array.clock_hand_animation_values
            )
        }
    }

    companion object {

        private const val SETTINGS_VERSION = 1
        private const val DEFAULT_LAYOUT = R.layout.view_digital_default

        const val SHARED_PREFERENCES_NAME = "com.bopr.piclock_preferences"

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
        const val PREF_CONTENT_COLORS = "content_colors"
        const val PREF_DATE_FORMAT = "date_format"
        const val PREF_DIGITS_ANIMATION = "digits_animation"
        const val PREF_DIGITS_SPLIT_ANIMATION = "digits_split_animation"
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