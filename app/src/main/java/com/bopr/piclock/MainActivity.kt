package com.bopr.piclock

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity(ClockFragment::class), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var fullscreenSupport: FullscreenSupport

//    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onCreate(savedInstanceState, persistentState)
//        settings = Settings(this)
//
//        val fragment = (fragment as ClockFragment)
//
//        fullscreenSupport = FullscreenSupport(window)
////        fullscreenSupport.autoFullscreenDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
//        fullscreenSupport.onChange = { fragment.showControls(!it) }
//
//        fragment.requireView().setOnClickListener {
//            fullscreenSupport.toggle()
//        }
//
//        /* Trigger the fullscreen mode shortly after the activity has been
//         created, to briefly hint to the user that UI controls are available. */
//        fullscreenSupport.fullscreen = true
//
//        settings.registerOnSharedPreferenceChangeListener(this)
//    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        settings = Settings(this)

        val fragment = (fragment as ClockFragment)

        fullscreenSupport = FullscreenSupport(window)
        fullscreenSupport.autoFullscreenDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
        fullscreenSupport.onChange = { fragment.showControls(!it) }

        fragment.requireView().setOnClickListener {
            fullscreenSupport.toggle()
        }

        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls are available. */
        fullscreenSupport.fullscreen = true

        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_AUTO_FULLSCREEN_DELAY) {
            fullscreenSupport.autoFullscreenDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
        }
    }

}