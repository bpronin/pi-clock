package com.bopr.piclock.util.property

import android.graphics.PointF
import android.util.Property
import android.view.View

class PositionProperty : Property<View, PointF>(PointF::class.java, NAME) {

    override fun get(view: View?): PointF? {
        return view?.run {
            PointF(x, y)
        }
    }

    override fun set(view: View?, value: PointF) {
        view?.apply {
            x = value.x
            y = value.y
        }
    }

    companion object {

        const val NAME = "position"
    }
}