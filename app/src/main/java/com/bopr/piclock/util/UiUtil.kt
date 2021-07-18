package com.bopr.piclock.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.RectF
import android.os.Build
import android.text.InputType
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.bopr.piclock.R

/**
 * Miscellaneous UI utilities.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

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

fun View.resetRenderParams() = apply {
    alpha = 1f
    scaleX = 1f
    scaleY = 1f
    translationX = 0f
    translationY = 0f
    translationZ = 0f
    rotation = 0f
    rotationX = 0f
    rotationY = 0f
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        resetPivot()
    } else {
        pivotX = width / 2f
        pivotY = height / 2f
    }
}

fun Contextual.messageBox(text: String) {
    AlertDialog.Builder(requireContext()).apply {
        setTitle(R.string.app_name)
        setMessage(text)
        setPositiveButton(android.R.string.ok, null)
    }.show()
}

fun Contextual.passwordBox(message: String, onPositiveClose: (String) -> Unit) {
    AlertDialog.Builder(requireContext()).apply {
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

fun LayoutInflater.inflateWithTheme(
    @LayoutRes layoutRes: Int, root: ViewGroup?, attachToRoot: Boolean, @StyleRes styleRes: Int
): View {
    return cloneInContext(
        ContextThemeWrapper(context, styleRes)
    ).inflate(
        layoutRes,
        root,
        attachToRoot
    )
}

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

/**
 * Adds set of custom properties to the animator and its children if it is an animator set.
 * Property names should be qualified before.
 */
fun Animator.extendProperties(properties: Collection<Property<*, *>>) {
    forEachChild { child ->
        child.apply {
            if (this is ObjectAnimator) {
                properties.find { it.name == propertyName }?.also(::setProperty)
            }
        }
    }
}
