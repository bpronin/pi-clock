package com.bopr.piclock.util

import android.content.Context
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun is24HourLocale(): Boolean {
    val pattern = (DateFormat.getTimeInstance() as SimpleDateFormat).toPattern()
    return !pattern.lowercase(Locale.ROOT).contains("a")
}

fun Context.getResId(defType: String, resName: String): Int {
    if (resName.indexOf("/") != -1) {
        /* Resource name must not be fully qualified */
        return 0
    }
    return resources.getIdentifier(resName, defType, packageName)
}

/**
 * Returns name of resource ID (short).
 */
fun Context.getResName(resId: Int): String {
    resources.getResourceName(resId).run {
        return substring(lastIndexOf("/") + 1)
    }
}

fun Context.isResExists(defType: String, resourceName: String): Boolean {
    return getResId(defType, resourceName) != 0
}

fun Context.getResArrayValue(arrayResId: Int, index: Int): String {
    return resources.getStringArray(arrayResId)[index]
}

fun Context.isResArrayValueExists(arrayResId: Int, value: String): Boolean {
    return resources.getStringArray(arrayResId).indexOf(value) != -1
}

