package com.bopr.piclock.util.property

import android.util.Property
import android.view.View

class ScaleProperty : Property<View, Float>(Float::class.java, NAME) {

    override fun get(view: View?): Float? {
        return view?.scaleX
    }

    override fun set(view: View?, value: Float) {
        view?.apply {
            scaleX = value
            scaleY = scaleX
        }
    }

    companion object {

        const val NAME = "scale"
    }
}