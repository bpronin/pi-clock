package com.bopr.piclock

enum class ContentLayoutType(private val resIds: Set<Int>) {

    DIGITAL(
        setOf(
            R.layout.view_digital_default,
            R.layout.view_digital_simple,
            R.layout.view_digital_vertical_default,
            R.layout.view_digital_vertical_simple,
        )
    ),
    ANALOG_TEXT_DATE(
        setOf(
            R.layout.view_analog_text_date_min
        )
    ),
    ANALOG_BARS_DATE(
        setOf(
            R.layout.view_analog_bars_date_min
        )
    );

    companion object {

        fun layoutTypeOf(resId: Int): ContentLayoutType =
            values().find { it.resIds.contains(resId) }
                ?: throw IllegalArgumentException("Unregistered layout resource: $resId")
    }
}

