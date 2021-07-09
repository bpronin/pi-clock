package com.bopr.piclock.util.property

import android.util.Property
import android.view.View

val SCALE_PROPERTY by lazy {

    object : Property<View, Float>(Float::class.java, "scale") {

        override fun get(view: View?): Float? {
            return view?.scaleX
        }

        override fun set(view: View?, value: Float) {
            view?.apply {
                scaleX = value
                scaleY = scaleX
            }
        }
    }
}

val RELATIVE_TRANSITION_X_PROPERTY by lazy {

    object : Property<View, Float>(Float::class.java, "relativeTranslationX") {

        override fun get(view: View?): Float? {
            return view?.run { translationX / width } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationX = (value ?: 0f) * width }
        }
    }
}

val RELATIVE_TRANSITION_Y_PROPERTY by lazy {

    object : Property<View, Float>(Float::class.java, "relativeTranslationY") {

        override fun get(view: View?): Float? {
            return view?.run { translationY / height } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationY = (value ?: 0f) * height }
        }
    }
}