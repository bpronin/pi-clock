package com.bopr.piclock.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.RectF
import android.text.InputType
import android.util.Property
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bopr.piclock.R

/**
 * Miscellaneous UI and resources utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
//fun ViewGroup.forEachDeep(action: (view: View) -> Unit) {
//    forEach { view ->
//        if (view is ViewGroup) {
//            view.forEachDeep(action)
//        } else {
//            action(view)
//        }
//    }
//}

fun RectF.scaled(factorX: Float, factorY: Float): RectF {
    if (factorX != 1f && factorY != 1f) {
        inset(width() * (1 - factorX) / 2, height() * (1 - factorY) / 2)
    }
    return this
}

fun RectF.scaled(factor: Float): RectF {
    return scaled(factor, factor)
}

val View.rect get() = RectF(x, y, x + width.toFloat(), y + height.toFloat())

val View.scaledRect get() = rect.scaled(scaleX, scaleY)

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

//fun AnimatorSet.findChildByProperty(propertyName: String): ObjectAnimator? {
//    for (child in childAnimations) {
//        if (child is AnimatorSet) {
//            child.findChildByProperty(propertyName)?.run {
//                return this
//            }
//        } else if (child is ObjectAnimator && child.propertyName == propertyName) {
//            return child
//        }
//    }
//    return null
//}

fun Animator.forEachChild(action: (Animator) -> Unit) {
    if (this is AnimatorSet) {
        for (child in childAnimations) {
            if (child is AnimatorSet) {
                child.forEachChild(action)
            } else {
                action(child)
            }
        }
    } else {
        action(this)
    }
}

fun Animator.extendProperties(properties: Collection<Property<*, *>>) {
    forEachChild { child ->
        child.apply {
            if (this is ObjectAnimator) {
                properties.find { it.name == propertyName }?.also(::setProperty)
            }
        }
    }
}
