package com.bopr.piclock.util.property

import android.util.Property
import android.view.View

class RelativeTransitionXProperty : Property<View, Float>(Float::class.java, NAME) {

    override fun get(view: View?): Float? {
        return view?.run { translationX / width } ?: run { null }
    }

    override fun set(view: View?, value: Float?) {
        view?.apply { translationX = (value ?: 0f) * width }
    }

    companion object {

        const val NAME = "relative_translationX"
    }
}

