package com.bopr.piclock.util

import android.content.Context

fun Context.getResourceId(defType: String, resName: String): Int {
    if (resName.indexOf("/") != -1) {
        /* Resource name must not be fully qualified */
        return 0
    }
    return resources.getIdentifier(resName, defType, packageName)
}

/**
 * Returns name of resource ID (short).
 */
fun Context.getResourceName(resId: Int): String {
    resources.getResourceName(resId).run {
        return substring(lastIndexOf("/") + 1)
    }
}

fun Context.isResourceExists(defType: String, resourceName: String): Boolean {
    return getResourceId(defType, resourceName) != 0
}

fun Context.getStringArray(resId: Int): Array<out String> {
    return resources.getStringArray(resId)
}

