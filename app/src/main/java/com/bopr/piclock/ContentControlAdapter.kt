package com.bopr.piclock

import android.content.Context
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.Contextual
import java.util.*

/**
 * Convenience empty implementation of [ContentControl].
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal abstract class ContentControlAdapter(val settings: Settings) : ContentControl,
    Contextual {

    @Mode
    protected var mode = MODE_ACTIVE
        private set

    override fun requireContext(): Context {
        return settings.requireContext()
    }

    override fun onTimer(time: Date, tick: Int) {
        /* does nothing by default */
    }

    override fun onSettingChanged(key: String) {
        /* does nothing by default */
    }

    override fun onModeChanged(@Mode newMode: Int, animate: Boolean) {
        mode = newMode
    }
}