package com.bopr.piclock.util

import android.app.AlertDialog
import android.content.Context
import android.graphics.RectF
import android.text.InputType
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.EditText
import androidx.annotation.IdRes
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

fun View.setOnLayouotCompleteListener(action: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {

        /** At this point the layout is complete and the dimensions
         * of the view and any child views are known.*/
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    })
}

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
//    val sw = width * scaleX
//    val sh = height * scaleY
//    val dx = (sw - width) / 2
//    val dy = (sh - height) / 2
//
//    val rectF = RectF(0f, 0f, sw, sh).apply {
//        offset(dx, dy)
//    }
//    return rectF
}

fun View.getParentScaledRect(): RectF {
    return (parent as View).getScaledRect()
}

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
