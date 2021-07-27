package com.bopr.piclock.util

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.piclock.R
import org.junit.Assert.assertEquals
import org.junit.Test


internal class ResourcesTest : Contextual {

    override fun requireContext(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    private fun assertResArraysSizeEqual(@ArrayRes vararg arrayResIds: Int) {
        val firstResId = arrayResIds[0]
        requireStringArray(firstResId).apply {
            for (arrayResId in arrayResIds) {
                assertEquals(
                    "[${getResName(firstResId)}] and [${getResName(arrayResId)}] sizes do not match ",
                    size, requireStringArray(arrayResId).size
                )
            }
        }
    }

    /**
     * Tests that related resource arrays (e.g. values-titles) sizes are equal.
     */
    @Test
    fun testArraysSizes() {
        assertResArraysSizeEqual(
            R.array.content_layout_values,
            R.array.content_layout_titles,
            R.array.content_layout_styles,
            R.array.content_layout_styles_values,
            R.array.content_layout_styles_titles,
            R.array.content_layout_colors_values,
            R.array.content_layout_colors_titles
        )
        assertResArraysSizeEqual(
            R.array.hours_minutes_format_values,
            R.array.hours_minutes_format_titles,
            R.array.hours_minutes_format_hints
        )
        assertResArraysSizeEqual(
            R.array.seconds_format_values,
            R.array.seconds_format_titles,
            R.array.seconds_format_hints
        )
        assertResArraysSizeEqual(
            R.array.auto_inactivate_delay_values,
            R.array.auto_inactivate_delay_titles
        )
        assertResArraysSizeEqual(
            R.array.content_float_interval_values,
            R.array.content_float_interval_titles
        )
        assertResArraysSizeEqual(
            R.array.content_float_speed_values,
            R.array.content_float_speed_titles
        )
        assertResArraysSizeEqual(
            R.array.tick_sound_mode_values,
            R.array.tick_sound_mode_titles
        )
        assertResArraysSizeEqual(
            R.array.tick_sound_values,
            R.array.tick_sound_titles
        )
        assertResArraysSizeEqual(
            R.array.digits_animation_values,
            R.array.digits_animation_titles
        )
        assertResArraysSizeEqual(
            R.array.float_animation_values,
            R.array.float_animation_titles
        )
        assertResArraysSizeEqual(
            R.array.clock_hand_animation_values,
            R.array.clock_hand_animation_titles
        )
    }

    /**
     * Tests that all resources defined in arrays exist.
     */
    @Test
    fun testArraysContent() {
        requireRefArray(R.array.content_layout_values).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.content_layout_titles).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.content_layout_styles).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.content_layout_styles_values).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.content_layout_styles_titles).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.content_layout_colors_values).forEach {
            it?.run { requireResId(it) }
        }
        requireRefArray(R.array.content_layout_colors_titles).forEach {
            it?.run { requireResId(it) }
        }
        requireRefArray(R.array.tick_sound_values).forEach {
            requireResId(it!!)
        }
        requireRefArray(R.array.digits_animation_values).forEach {
            it?.run { requireResId(it) }
        }
        requireRefArray(R.array.float_animation_values).forEach {
            it?.run { requireResId(it) }
        }
        requireRefArray(R.array.clock_hand_animation_values).forEach {
            it?.run { requireResId(it) }
        }
    }

    /**
     * Tests that all layouts styles resources exist.
     */
    @Test
    fun testLayoutStyles() {
        requireRefArray(R.array.content_layout_values).forEachIndexed { layoutIndex, layoutName ->
            val baseStyleName = requireRefArray(R.array.content_layout_styles)[layoutIndex]
            val styleNames = requireStringArray(requireStyleValuesResId(layoutName))
            val colorsResId = getColorsValuesResId(layoutName)
            if (colorsResId != 0) {
                requireStringArray(colorsResId).forEach { colorName ->
                    styleNames.forEach { styleName ->
                        requireResId(baseStyleName + styleName + colorName)
                    }
                }
            } else {
                styleNames.forEach { styleName ->
                    requireResId(baseStyleName + styleName)
                }
            }
        }
    }
}