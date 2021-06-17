package com.bopr.piclock

import androidx.fragment.app.Fragment
import com.bopr.piclock.util.ui.BaseActivity

/**
 * Debugging actions activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugActivity : BaseActivity() {

    override fun onCreateFragment(): Fragment {
        return DebugFragment()
    }
}