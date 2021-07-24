package com.bopr.piclock

enum class ContentLayoutType(internal val prefix: String) {

    DIGITAL("view_digital"),
    ANALOG_TEXT_DATE("view_analog_text_date"),
    ANALOG_BARS_DATE("view_analog_bars_date");

    companion object {

        fun layoutTypeOf(name: String): ContentLayoutType {
            return values().find { name.startsWith(it.prefix) }
                ?: throw IllegalArgumentException("Invalid layout name: $name")
        }
    }
}

