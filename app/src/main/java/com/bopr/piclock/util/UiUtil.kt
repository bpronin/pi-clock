package com.bopr.piclock.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.RectF
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun <T : View?> View.requireViewByIdCompat(@IdRes id: Int): T {
    return findViewById(id)
        ?: throw IllegalArgumentException("ID does not reference a View inside this View")
}

fun View.getScaledRect(): RectF {
    val w = width * scaleX
    val h = height * scaleY
    return RectF().apply {
        left = (width - w) / 2
        right = left + w
        top = (height - h) / 2
        bottom = top + h
    }
}

fun Context.messageBox(text: String) {
    AlertDialog.Builder(this).apply {
        setTitle(R.string.app_name)
        setMessage(text)
        setPositiveButton(android.R.string.ok, null);
    }.show()
}

fun Context.passwordBox(message: String, onPositiveClose: (String) -> Unit) {
    AlertDialog.Builder(this).apply {
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        setTitle(R.string.app_name)
        setMessage(message)
        setView(input)
        setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
        setPositiveButton(android.R.string.ok) { _, _ ->
            onPositiveClose(input.text.toString())
        }
    }.show()
}
