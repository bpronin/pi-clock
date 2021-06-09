package com.bopr.piclock

import android.os.Bundle

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity(ClockFragment::class) {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val fragment = (fragment as ClockFragment)

        val fullscreenSupport = FullscreenSupport(window) {
            fragment.showControls(!it)
        }

        fragment.view?.setOnClickListener {
            fullscreenSupport.toggle()
        }

        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls are available. */
        fullscreenSupport.fullscreen = true
    }

}