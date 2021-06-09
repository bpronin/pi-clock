package com.bopr.piclock.util

import com.bopr.piclock.Settings.Companion.DATE_FORMAT_DATE
import com.bopr.piclock.Settings.Companion.DATE_FORMAT_LONG
import com.bopr.piclock.Settings.Companion.DATE_FORMAT_SHORT
import java.text.SimpleDateFormat
import java.util.*

fun clockDateFormat(value: String): SimpleDateFormat {
    val pattern = when (value) {
        DATE_FORMAT_DATE -> "yyyy-MM-dd"
        DATE_FORMAT_LONG -> "EEEE, MMMM dd"
        DATE_FORMAT_SHORT -> "EEE, MMM d"
        else -> "yyyy-MM-dd"
    }
    return SimpleDateFormat(pattern, Locale.getDefault())
}

