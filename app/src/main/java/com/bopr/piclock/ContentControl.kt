package com.bopr.piclock

import com.bopr.piclock.MainFragment.Mode
import java.util.*

/**
 * Subclasses of this class encapsulate different aspects of main fragment's behaviour.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
interface ContentControl {

    fun onTimer(time: Date, tick: Int)

    fun onSettingChanged(key: String)

    fun onModeChanged(@Mode newMode: Int, animate: Boolean)
}