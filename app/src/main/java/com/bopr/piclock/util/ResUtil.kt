package com.bopr.piclock.util

import android.content.Context

fun Context.getResourceId(defType: String, resName: String): Int {
    return resources.getIdentifier(resName, defType, packageName)
}

fun Context.getResourceName(resId: Int): String {
    return resources.getResourceName(resId)
}
fun Context.isResourceExists(defType: String, resourceName: String): Boolean {
    return getResourceId(defType, resourceName) != 0
}

fun Context.getStringArray(resId: Int): Array<out String> {
    return resources.getStringArray(resId)
}

