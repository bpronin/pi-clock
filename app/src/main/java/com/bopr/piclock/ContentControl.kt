package com.bopr.piclock

import java.util.*

interface ContentControl {

    fun onTimer(time: Date, tick: Int)

    fun onSettingChanged(key: String)

}