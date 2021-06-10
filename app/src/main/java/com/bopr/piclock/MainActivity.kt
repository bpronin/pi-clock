package com.bopr.piclock

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity(ClockFragment::class), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var fullscreenSupport: FullscreenSupport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = Settings(this)
        settings.loadDefaults()

        fullscreenSupport = FullscreenSupport(window)
//        fullscreenSupport.autoFullscreenDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
        fullscreenSupport.onChange = {
            (fragment as ClockFragment).showControls(!it) //todo:get rid of cast
        }

        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        fragment!!.requireView().setOnClickListener {
            fullscreenSupport.toggle()
        }
        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls are available. */
        fullscreenSupport.fullscreen = true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
//        if (key == PREF_AUTO_FULLSCREEN_DELAY) {
//            fullscreenSupport.autoFullscreenDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
//        }
    }

}