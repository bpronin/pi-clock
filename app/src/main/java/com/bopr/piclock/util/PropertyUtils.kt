package com.bopr.piclock.util

import android.util.Property
import android.view.View

val RELATIVE_TRANSLATION_Y by lazy {

    object : Property<View, Float>(Float::class.java, "relative_translationY") {

        override fun get(view: View?): Float? {
            return view?.run { translationY / height } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationY = (value ?: 0f) * height }
        }
    }
}

val RELATIVE_TRANSLATION_X by lazy {

    object : Property<View, Float>(Float::class.java, "relative_translationX") {

        override fun get(view: View?): Float? {
            return view?.run { translationX / width } ?: run { null }
        }

        override fun set(view: View?, value: Float?) {
            view?.apply { translationX = (value ?: 0f) * width }
        }
    }
}
