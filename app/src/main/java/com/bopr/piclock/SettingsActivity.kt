package com.bopr.piclock

import androidx.fragment.app.Fragment
import com.bopr.piclock.util.ui.BaseActivity

/**
 * Settings activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SettingsActivity : BaseActivity() {

    override fun onCreateFragment(): Fragment {
        return SettingsFragment()
    }
}