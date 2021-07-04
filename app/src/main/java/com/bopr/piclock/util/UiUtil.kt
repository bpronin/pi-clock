package com.bopr.piclock.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.RectF
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.forEach
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun ViewGroup.forEachDeep(action: (view: View) -> Unit) {
    forEach { view ->
        if (view is ViewGroup) {
            view.forEachDeep(action)
        } else {
            action(view)
        }
    }
}

//@Suppress("DEPRECATION")
//fun getSystemInsetsCompat(insets: WindowInsets): Rect {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        val sbInsets = insets.getInsets(WindowInsets.Type.systemBars())
//        return Rect(
//            sbInsets.left,
//            sbInsets.top,
//            sbInsets.right,
//            sbInsets.bottom
//        )
//    } else {
//        return Rect(
//            insets.systemWindowInsetLeft,
//            insets.systemWindowInsetTop,
//            insets.systemWindowInsetRight,
//            insets.systemWindowInsetBottom
//        )
//    }
//}

fun RectF.scale(factorX: Float, factorY: Float) {
    if (factorX != 1f && factorY != 1f) {
        inset(width() * (1 - factorX) / 2, height() * (1 - factorY) / 2)
    }
}

val View.rect get() = RectF(x, y, x + width.toFloat(), y + height.toFloat())

val View.scaledRect get() = rect.apply { scale(scaleX, scaleY) }

val View.parentView get() = (parent as ViewGroup)

fun Context.messageBox(text: String) {
    AlertDialog.Builder(this).apply {
        setTitle(R.string.app_name)
        setMessage(text)
        setPositiveButton(android.R.string.ok, null)
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
