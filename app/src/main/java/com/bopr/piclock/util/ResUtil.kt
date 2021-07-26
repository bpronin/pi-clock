package com.bopr.piclock.util

import androidx.annotation.*
import com.bopr.piclock.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

private const val RAW = "raw"
private const val ANIMATOR = "animator"
private const val LAYOUT = "layout"
private const val STYLE = "style"
private const val ARRAY = "array"

/**
 * Miscellaneous resource utilities.
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

fun defaultDatetimeFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())

/**
 * Returns ID of resource by its name.
 */
private fun Contextual.getResId(resType: String, resName: String): Int {
    if (resName.indexOf("/") != -1) {
        throw Error("Resource name must NOT be fully qualified")
    }
    return requireContext().run {
        resources.getIdentifier(resName, resType, packageName)
    }
}

/**
 * Returns ID of resource by its name or throws an exception when resource does not exist.
 */
private fun Contextual.requireResId(resType: String, resName: String): Int {
    return getResId(resType, resName).also {
        if (it == 0) throw Error("Resource does not exist: $resType/$resName")
    }
}

fun Contextual.requireResArray(@ArrayRes resId: Int): Array<out String> {
    return requireContext().resources.getStringArray(resId)
}

/**
 * Returns name of resource ID (short).
 */
fun Contextual.getResName(@AnyRes resId: Int): String {
    return requireContext().resources.getResourceEntryName(resId)
}

@RawRes
fun Contextual.requireRawResId(resName: String) = requireResId(RAW, resName)

@AnimatorRes
fun Contextual.requireAnimatorResId(resName: String) = requireResId(ANIMATOR, resName)

@LayoutRes
fun Contextual.requireLayoutResId(resName: String) = requireResId(LAYOUT, resName)

@StyleRes
fun Contextual.getStyleResId(resName: String) = getResId(STYLE, resName)

@StyleRes
fun Contextual.requireStyleResId(resName: String) = requireResId(STYLE, resName)

@ArrayRes
fun Contextual.getArrayResId(resName: String) = getResId(ARRAY, resName)

@ArrayRes
fun Contextual.requireArrayResId(resName: String) = requireResId(ARRAY, resName)

/**
 * Returns true if resource array contains specified value.
 */
fun <T> Contextual.isResArrayContains(@ArrayRes arrayResId: Int, value: T): Boolean {
    return requireResArray(arrayResId).contains(value.toString())
}

/**
 * Returns true if resource array contains all specified values.
 */
fun <V, C : Collection<V>> Contextual.isResArrayContainsAll(
    @ArrayRes arrayResId: Int,
    values: C
): Boolean {
    val array = requireResArray(arrayResId)
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
fun <T> Contextual.ensureResArrayContains(@ArrayRes arrayResId: Int, value: T): T {
    if (!isResArrayContains(arrayResId, value)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

/**
 * Throws an exception if resource array does not contain all specified values.
 */
fun <C : Collection<*>> Contextual.ensureResArrayContainsAll(
    @ArrayRes arrayResId: Int,
    values: C
): C {
    if (!isResArrayContainsAll(arrayResId, values)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

@ArrayRes
fun Contextual.requireStyleValuesResId(@LayoutRes layoutResId: Int): Int {
    return requireArrayResId(getResName(layoutResId) + "_style_values")
}

@ArrayRes
fun Contextual.requireStyleTitlesResId(@LayoutRes layoutResId: Int): Int {
    return requireArrayResId(getResName(layoutResId) + "_style_titles")
}

@ArrayRes
fun Contextual.getColorsValuesResId(@LayoutRes layoutResId: Int): Int {
    return getArrayResId(getResName(layoutResId) + "_colors_values")
}

@ArrayRes
fun Contextual.getColorsTitlesResId(@LayoutRes layoutResId: Int): Int {
    return getArrayResId(getResName(layoutResId) + "_colors_titles")
}

fun Contextual.getLayoutStyleName(layoutName: String, style: String, color: String): String {
    val layoutIndex = requireResArray(R.array.content_layout_values).indexOf(layoutName)
    val stylePrefix = requireResArray(R.array.content_layout_styles)[layoutIndex]
    return stylePrefix + style + color
}

///**
// * Returns ID of resource by its fully qualified path.
// */
//fun Contextual.getResId(resPath: String): Int {
//    return requireContext().run {
//        val resName = resPath.substringAfter("res/").substringBeforeLast(".")
//        resources.getIdentifier(resName, null, packageName)
//    }
//}

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
