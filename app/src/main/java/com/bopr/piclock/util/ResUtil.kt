package com.bopr.piclock.util

import android.util.TypedValue.TYPE_NULL
import androidx.annotation.AnyRes
import androidx.annotation.ArrayRes
import com.bopr.piclock.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

/**
 * Miscellaneous resource utilities and constants.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

const val SYSTEM_DEFAULT = "system_default"

val DEFAULT_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL)

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
 * Returns ID of resource by its long name of the form "type/entry".
 */
private fun Contextual.getResId(resName: String?): Int {
    return requireContext().run {
        resName?.run { resources.getIdentifier(this, null, packageName) } ?: 0
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

fun Contextual.isResExists(resName: String): Boolean {
    return getResId(resName) != 0
}

/**
 * Returns long name of resource of the form "type/entry" or throws ana exception
 * if resource does no exists.
 */
fun Contextual.getResName(@AnyRes resId: Int): String {
    return requireContext().resources.run {
        "${getResourceTypeName(resId)}/${getResourceEntryName(resId)}"
    }
}

fun Contextual.requireStringArray(@ArrayRes resId: Int): Array<String> {
    return requireContext().resources.getStringArray(resId)
}

/**
 * Returns true if resource array contains specified value.
 */
fun <T> Contextual.isStringArrayContains(@ArrayRes arrayResId: Int, value: T): Boolean {
    return requireStringArray(arrayResId).contains(value.toString())
}

/**
 * Returns true if resource array contains all specified values.
 */
fun <V, C : Collection<V>> Contextual.isStringArrayContainsAll(
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
        throw Error("Resource array: ${getResName(arrayResId)} does not contain value: $value")
    } else {
        return value
    }
}

/**
 * Throws an exception if resource array does not contain all specified values.
 */
fun <C : Collection<*>> Contextual.ensureStringArrayContainsAll(
    @ArrayRes arrayResId: Int,
    values: C
): C {
    if (!isStringArrayContainsAll(arrayResId, values)) {
        throw Error("Resource array: ${getResName(arrayResId)} does not contain values: $values")
    } else {
        return values
    }
}

fun Contextual.requireRefArray(@ArrayRes arrayResId: Int): Array<String?> {
    return requireContext().resources.obtainTypedArray(arrayResId).run {
        val array = Array(length()) { index ->
            if (getType(index) == TYPE_NULL)
                null
            else getResourceId(index, 0).run {
                if (this == 0) throw Error(
                    "Expected reference or @null at position: $index in array: " +
                            "${getResName(arrayResId)} "
                )
                getResName(this)
            }
        }
        recycle()
        array
    }
}

fun Contextual.isRefArrayContains(@ArrayRes arrayResId: Int, resName: String): Boolean {
    return requireRefArray(arrayResId).contains(resName)
}

fun Contextual.ensureRefArrayContains(
    @ArrayRes arrayResId: Int,
    resName: String
): String {
    if (!isRefArrayContains(arrayResId, resName)) {
        throw Error(
            "Resource array: ${getResName(arrayResId)} does not contain value: $resName"
        )
    } else {
        return resName
    }
}

private fun Contextual.getArrayMapping(
    @ArrayRes keysArrayResId: Int,
    @ArrayRes valuesArrayResId: Int,
    key: String?
) = requireRefArray(keysArrayResId).indexOf(key).run {
    if (this != -1) requireRefArray(valuesArrayResId)[this] else null
}

private fun Contextual.requireArrayMapping(
    @ArrayRes keysArrayResId: Int,
    @ArrayRes valuesArrayResId: Int,
    key: String?
) = getArrayMapping(keysArrayResId, valuesArrayResId, key)
    ?: throw Error("Array mapping does not exist for key: $key")

@ArrayRes
fun Contextual.requireStyleValuesResId(layoutName: String?) = requireResId(
    requireArrayMapping(
        R.array.content_layout_values, R.array.content_layout_styles_values, layoutName
    )
)

@ArrayRes
fun Contextual.requireStyleTitlesResId(layoutName: String?) = requireResId(
    requireArrayMapping(
        R.array.content_layout_values, R.array.content_layout_styles_titles, layoutName
    )
)

@ArrayRes
fun Contextual.getColorsValuesResId(layoutName: String?) = getResId(
    getArrayMapping(
        R.array.content_layout_values, R.array.content_layout_colors_values, layoutName
    )
)

@ArrayRes
fun Contextual.requireColorsTitlesResId(layoutName: String?) = requireResId(
    requireArrayMapping(
        R.array.content_layout_values, R.array.content_layout_colors_titles, layoutName
    )
)

fun Contextual.getLayoutStyleName(
    layoutName: String,
    styleName: String,
    colorName: String
): String {
    val layoutPrefix = getArrayMapping(
        R.array.content_layout_values, R.array.content_layout_styles, layoutName
    ) ?: ""

    val styleSuffix = getArrayMapping(
        R.array.content_layout_values, R.array.content_layout_styles_values, layoutName
    )?.let {
        requireStringArray(requireResId(it)).let { styles ->
            if (styles.contains(styleName)) styleName else styles.first()
        }
    } ?: ""

    val colorSuffix = getArrayMapping(
        R.array.content_layout_values, R.array.content_layout_colors_values, layoutName
    )?.let {
        requireStringArray(requireResId(it)).let { colors ->
            if (colors.contains(colorName)) colorName else colors.first()
        }
    } ?: ""


    return layoutPrefix + styleSuffix + colorSuffix
}

///**
// * Returns resource fully qualified path in form of "res/drawable/pic.jpg"
// */
//fun Contextual.getResPath(resId: Int): String {
//    val value = TypedValue()
//    requireContext().resources.getValue(resId, value, true)
//    return value.string.toString()
//}
