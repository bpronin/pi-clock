package com.bopr.piclock

import java.util.*

/**
 * Wraps real [ContentControl] to hold its reference.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class ContentControlWrapper : ContentControl {

    private lateinit var control: ContentControl

    fun setControl(control: ContentControl) {
        this.control = control
    }

    override fun onTimer(time: Date, tick: Int) {
        control.onTimer(time, tick)
    }

    override fun onSettingChanged(key: String) {
        control.onSettingChanged(key)
    }

    override fun onModeChanged(newMode: Int, animate: Boolean) {
        control.onModeChanged(newMode, animate)
    }

}