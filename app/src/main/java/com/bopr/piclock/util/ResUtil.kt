package com.bopr.piclock.util

import androidx.annotation.AnyRes
import androidx.annotation.ArrayRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.bopr.piclock.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

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
 * Returns ID of resource by its short name and type.
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
 * Returns ID of resource by its long name of the form "type/entry".
 */
private fun Contextual.getResId(resName: String): Int {
    return requireContext().run {
        resources.getIdentifier(resName, null, packageName)
    }
}

/**
 * Returns name of resource ID (short).
 */
fun Contextual.getResShortName(@AnyRes resId: Int): String {
    return requireContext().resources.getResourceEntryName(resId)
}

/**
 * Returns long name of resource of the form "type/entry" or null
 * if resource is invalid.
 */
fun Contextual.getResName(@AnyRes resId: Int): String? {
    return if (resId == 0)
        null
    else requireContext().resources.run {
        "${getResourceTypeName(resId)}/${getResourceEntryName(resId)}"
    }
}

/**
 * Returns long name of resource of the form "type/entry" or throws ana exception
 * if resource does no exists.
 */
fun Contextual.requireResName(@AnyRes resId: Int): String {
    return requireContext().resources.run {
        "${getResourceTypeName(resId)}/${getResourceEntryName(resId)}"
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

/**
 * Returns ID of resource by its long name or throws an exception when resource does not exist.
 */
fun Contextual.requireResId(resName: String): Int {
    return getResId(resName).also {
        if (it == 0) throw Error("Resource does not exist: $resName")
    }
}

fun Contextual.requireStringArray(@ArrayRes resId: Int): Array<String> {
    return requireContext().resources.getStringArray(resId)
}

fun Contextual.requireTypedArray(@ArrayRes resId: Int): Array<String?> {
    return requireContext().resources.obtainTypedArray(resId).run {
        val array = Array(length()) {
            getResName(getResourceId(it, 0))
        }
        recycle()
        array
    }
}

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
fun <T> Contextual.isStringArrayContains(@ArrayRes arrayResId: Int, value: T): Boolean {
    return requireStringArray(arrayResId).contains(value.toString())
}

fun Contextual.isTypedArrayContains(@ArrayRes arrayResId: Int, resName: String): Boolean {
    return requireTypedArray(arrayResId).contains(resName)
}

/**
 * Returns true if resource array contains all specified values.
 */
fun <V, C : Collection<V>> Contextual.isResArrayContainsAll(
    @ArrayRes arrayResId: Int,
    values: C
): Boolean {
    val array = requireStringArray(arrayResId)
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
fun <T> Contextual.ensureStringArrayContains(@ArrayRes arrayResId: Int, value: T): T {
    if (!isStringArrayContains(arrayResId, value)) {
        throw Error("Resource array: ${getResShortName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

fun Contextual.ensureTypedResArrayContains(
    @ArrayRes arrayResId: Int,
    resName: String
): String {
    if (!isTypedArrayContains(arrayResId, resName)) {
        throw Error(
            "Resource array: ${getResName(arrayResId)} does not contain value: $resName"
        )
    } else {
        return resName
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
        throw Error("Resource array: ${getResShortName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

@ArrayRes
fun Contextual.getStyleValuesResId(@LayoutRes layoutResId: Int): Int =
    getArrayResId(getResShortName(layoutResId) + "_style_values")

@ArrayRes
fun Contextual.requireStyleValuesResId(@LayoutRes layoutResId: Int): Int =
    requireArrayResId(getResShortName(layoutResId) + "_style_values")

@ArrayRes
fun Contextual.requireStyleTitlesResId(@LayoutRes layoutResId: Int): Int =
    requireArrayResId(getResShortName(layoutResId) + "_style_titles")

@ArrayRes
fun Contextual.getColorsValuesResId(@LayoutRes layoutResId: Int): Int =
    getArrayResId(getResShortName(layoutResId) + "_colors_values")

@ArrayRes
fun Contextual.getColorsTitlesResId(@LayoutRes layoutResId: Int): Int =
    getArrayResId(getResShortName(layoutResId) + "_colors_titles")

fun Contextual.getLayoutStyleName(layoutResName: String, style: String, color: String): String {
    val layoutPrefix = requireTypedArray(R.array.content_layout_values).run {
        val layoutIndex = indexOf(layoutResName)
        if (layoutIndex != -1)
            requireStringArray(R.array.content_layout_styles)[layoutIndex]
        else ""
    }

    val layoutResId = requireResId(layoutResName)

    val styleSuffix = getStyleValuesResId(layoutResId).let { stylesResId ->
        if (stylesResId != 0) requireStringArray(stylesResId).let { styles ->
            if (styles.contains(style)) style else styles[0]
        } else ""
    }

    val colorSuffix = getColorsValuesResId(layoutResId).let { colorsResId ->
        if (colorsResId != 0) requireStringArray(colorsResId).let { colors ->
            if (colors.contains(color)) color else colors[0]
        } else ""
    }

    return layoutPrefix + styleSuffix + colorSuffix
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

//fun Contextual.getResRef(resId: Int): String {
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
