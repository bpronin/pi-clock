package com.bopr.piclock

import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Mode
import java.util.*

/**
 * Subclasses of this class encapsulate different aspects of main fragment's behaviour.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal abstract class ContentControl(protected val settings: Settings) {

    @Mode
    protected var mode = MODE_ACTIVE
        private set

    open fun onTimer(time: Date, tick: Int) {
        /* does nothing by default */
    }

    open fun onSettingChanged(key: String) {
        /* does nothing by default */
    }

    open fun onModeChanged(@Mode newMode: Int, animate: Boolean) {
        mode = newMode
    }
}