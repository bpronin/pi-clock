package com.bopr.piclock.util

import android.content.Context
import androidx.fragment.app.Fragment
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

fun <V, C : Collection<V>> Context.isAllResExists(arrayResId: Int, values: C): Boolean {
    val array = resources.getStringArray(arrayResId)
    for (value in values) {
        if (!array.contains(value.toString())) {
            return false
        }
    }
    return true
}

/**
 * Throws an exception if resource array does not contain value.
 */
fun <T> Context.ensureResExists(arrayResId: Int, value: T): T {
    if (!isResExists(arrayResId, value)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

fun <V, C : Collection<V>> Context.ensureAllResExists(arrayResId: Int, values: C): C {
    if (!isAllResExists(arrayResId, values)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

fun Fragment.getStringArray(resId: Int): Array<out String> {
    return resources.getStringArray(resId)
}

fun defaultDatetimeFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())