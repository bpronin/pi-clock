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

/**
 * Returns true if resource array contains value.
 */
fun <T> Context.isResExists(arrayResId: Int, value: T): Boolean {
    return resources.getStringArray(arrayResId).contains(value.toString())
}

/**
 * Throws an exception if resource array does not contain value.
 */
fun <T> Context.ensureResExists(arrayResId: Int, value: T): T {
    if (!isResExists(arrayResId, value)) {
        throw Error("Resource array value does not exists: $value")
    } else {
        return value
    }
}

