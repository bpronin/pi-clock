package com.bopr.piclock.util

import android.util.Property
import android.view.View

class RelativeTranslationYProperty : Property<View, Float>(Float::class.java, NAME) {

    override fun get(view: View?): Float? {
        return view?.run { translationY / height } ?: run { null }
    }

    override fun set(view: View?, value: Float) {
        view?.apply { translationY = value * height }
    }

    companion object {

        const val NAME = "relative_translationY"
    }
}