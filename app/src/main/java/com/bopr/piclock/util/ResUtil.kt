package com.bopr.piclock.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK


/**
 * Miscellaneous resourceconstants and  utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

val localeIs24Hour: Boolean
    get() {
        val pattern = (DateFormat.getTimeInstance() as SimpleDateFormat).toPattern()
        return !pattern.lowercase().contains("a")
    }

val localeFirstDayOfWeek: Int get() = Calendar.getInstance().firstDayOfWeek

fun dayOfWeek(date: Date): Int = Calendar.getInstance().run {
    time = date
    get(DAY_OF_WEEK)
}

/**
 * Returns ID of resource by its name.
 */
fun Contextual.getResId(defType: String, resName: String): Int {
    if (resName.indexOf("/") != -1) {
        throw IllegalArgumentException("Resource name must NOT be fully qualified")
    }
    return requireContext().run {
        resources.getIdentifier(resName, defType, packageName)
    }
}

/**
 * Returns ID of resource by its fully qualified path.
 */
fun Contextual.getResId(resPath: String): Int {
    return requireContext().run {
        val resName = resPath.substringAfter("res/").substringBeforeLast(".")
        resources.getIdentifier(resName, null, packageName)
    }
}

/**
 * Returns ID of resource by its name or throws an exception when resource does not exist.
 */
fun Contextual.requireResId(defType: String, resName: String): Int {
    val resId = getResId(defType, resName)
    if (resId == 0)
        throw IllegalArgumentException("Resource does not exist: $defType/$resName")
    return resId
}

/**
 * Returns name of resource ID (short).
 */
fun Contextual.getResName(resId: Int): String {
    return requireContext().resources.getResourceEntryName(resId)
}

//fun Contextual.getResFullName(resId: Int): String {
//    return requireContext().resources.run {
//        "@${getResourceTypeName(resId)}/${getResourceEntryName(resId)}"
//    }
//}

///**
// * Returns resource fully qualified path in form of "res/drawable/pic.jpg"
// */
//fun Contextual.getResPath(resId: Int): String {
//    val value = TypedValue()
//    requireContext().resources.getValue(resId, value, true)
//    return value.string.toString()
//}
//fun Contextual.getResArray(resId: Int): IntArray {
//    val array: IntArray
//    requireContext().resources.obtainTypedArray(resId).apply {
//        array = IntArray(length()) { getResourceId(it, 0) }
//        recycle()
//    }
//    return array
//}

/**
 * Returns true if resource array contains specified value.
 */
fun <T> Contextual.isResArrayContains(arrayResId: Int, value: T): Boolean {
    return getStringArray(arrayResId).contains(value.toString())
}

/**
 * Returns true if resource array contains all specified values.
 */
fun <V, C : Collection<V>> Contextual.isResArrayContainsAll(
    arrayResId: Int,
    values: C
): Boolean {
    val array = getStringArray(arrayResId)
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
fun <T> Contextual.ensureResArrayContains(arrayResId: Int, value: T): T {
    if (!isResArrayContains(arrayResId, value)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

/**
 * Throws an exception if resource array does not contain all specified values.
 */
fun <C : Collection<*>> Contextual.ensureAllResExists(arrayResId: Int, values: C): C {
    if (!isResArrayContainsAll(arrayResId, values)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

/**
 * Convenience function. Returns context's resource array.
 */

fun Contextual.getStringArray(resId: Int): Array<out String> {
    return requireContext().resources.getStringArray(resId)
}

fun Contextual.getStyleValuesResId(layoutResId: Int): Int {
    return requireResId("array", getResName(layoutResId) + "_style_values")
}

fun Contextual.getStyleTitlesResId(layoutResId: Int): Int {
    return requireResId("array", getResName(layoutResId) + "_style_titles")
}

fun Contextual.getLayoutStyles(layoutName: String): Array<out String> {
    return getStringArray(getStyleValuesResId(requireResId("layout", layoutName)))
}

fun defaultDatetimeFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())