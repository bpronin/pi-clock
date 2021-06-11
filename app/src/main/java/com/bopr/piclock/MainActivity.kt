package com.bopr.piclock

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings(this).validate()
    }

    override fun onCreateFragment(): Fragment {
        val fullscreenSupport = FullscreenSupport(window)
        val fragment = ClockFragment()

        fullscreenSupport.onChange = {
            fragment.setControlsVisible(!it)
        }

        fragment.onClick = {
            fullscreenSupport.toggle()
        }

        fullscreenSupport.fullscreen = true

        return fragment
    }

}