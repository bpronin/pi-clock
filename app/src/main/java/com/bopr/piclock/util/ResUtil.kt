package com.bopr.piclock.util

import android.content.Context
import android.content.res.Resources
import androidx.fragment.app.Fragment
import com.bopr.piclock.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Miscellaneous resourceconstants and  utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun is24HourLocale(): Boolean {
    val pattern = (DateFormat.getTimeInstance() as SimpleDateFormat).toPattern()
    return !pattern.lowercase(Locale.ROOT).contains("a")
}

/**
 * Returns ID of resource by its name.
 */
fun Context.getResId(defType: String, resName: String): Int {
    if (resName.indexOf("/") != -1) {
        throw IllegalArgumentException("Resource name must NOT be fully qualified")
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
 * Returns true if resource array contains specified value.
 */
fun <T> Context.isResArrayContains(arrayResId: Int, value: T): Boolean {
    return resources.getStringArray(arrayResId).contains(value.toString())
}

/**
 * Returns true if resource array contains all specified values.
 */
fun <V, C : Collection<V>> Context.isResArrayContainsAll(arrayResId: Int, values: C): Boolean {
    val array = resources.getStringArray(arrayResId)
    for (value in values) {
        if (!array.contains(value.toString())) {
            return false
        }
    }
    return true
}

/**
 * Throws an exception if resource array does not contain specified value.
 */
fun <T> Context.ensureResArrayContains(arrayResId: Int, value: T): T {
    if (!isResArrayContains(arrayResId, value)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

/**
 * Throws an exception if resource array does not contain all specified values.
 */
fun <C : Collection<*>> Context.ensureAllResExists(arrayResId: Int, values: C): C {
    if (!isResArrayContainsAll(arrayResId, values)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

fun Context.getStringArray(resId: Int): Array<out String> {
    return resources.getStringArray(resId)
}

fun Fragment.getStringArray(resId: Int): Array<out String> {
    return resources.getStringArray(resId)
}

fun defaultDatetimeFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())

val Resources.fabMargin get() = getDimension(R.dimen.fab_margin).toInt()