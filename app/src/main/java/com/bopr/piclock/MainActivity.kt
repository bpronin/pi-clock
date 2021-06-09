package com.bopr.piclock

import android.os.Bundle


class MainActivity : BaseActivity(ClockFragment::class) {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val fullscreenSupport = FullscreenSupport(window, fragment?.view)
        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls are available. */
        fullscreenSupport.fullscreen = true
    }

}