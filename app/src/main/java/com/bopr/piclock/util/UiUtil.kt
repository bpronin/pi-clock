package com.bopr.piclock.util

import android.view.View
import androidx.annotation.IdRes

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun <T : View?> View.requireViewByIdCompat(@IdRes id: Int): T {
    return findViewById(id)
        ?: throw IllegalArgumentException("ID does not reference a View inside this View")
}