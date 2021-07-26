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
                    "[${getResShortName(firstResId)}] and [${getResShortName(arrayResId)}] sizes do not match ",
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
            R.array.content_layout_styles
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
     * Tests resource arrays containing resource references.
     */
    @Test
    fun testArraysContent() {
        requireStringArray(R.array.content_layout_values).forEach {
            requireResId(it)
        }
        requireStringArray(R.array.tick_sound_values).forEach {
            if (it.isNotEmpty()) requireResId(it)
        }
        requireStringArray(R.array.digits_animation_values).forEach {
            if (it.isNotEmpty()) requireResId(it)
        }
        requireStringArray(R.array.float_animation_values).forEach {
            if (it.isNotEmpty()) requireResId(it)
        }
        requireStringArray(R.array.clock_hand_animation_values).forEach {
            if (it.isNotEmpty()) requireResId(it)
        }
    }

    /**
     * Tests that all layouts styles exist.
     */
    @Test
    fun testLayoutStyles() {
        requireStringArray(R.array.content_layout_styles).forEachIndexed { index, stylePrefix ->
            val layoutResId =
                requireResId(requireStringArray(R.array.content_layout_values)[index])
            val styles = requireStringArray(requireStyleValuesResId(layoutResId))
            val colorsId = getColorsValuesResId(layoutResId)
            if (colorsId != 0) {
                val colors = requireStringArray(colorsId)
                styles.forEach { style ->
                    colors.forEach { color ->
                        requireStyleResId(stylePrefix + style + color)
                    }
                }
            } else {
                styles.forEach { style ->
                    requireStyleResId(stylePrefix + style)
                }
            }
        }
    }
}