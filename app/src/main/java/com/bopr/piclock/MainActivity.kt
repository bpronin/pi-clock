package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY

/**
 * Main activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var fullscreenControl: FullscreenSupport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = Settings(this)
        settings.validate()
        settings.registerOnSharedPreferenceChangeListener(this)

        fullscreenControl = FullscreenSupport(window)
        updateFullscreenControl()
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateFragment(): Fragment {
        val fragment = ClockFragment()

        fullscreenControl.onChange = {
            fragment.setActive(!it)
        }

        fragment.onClick = {
            fullscreenControl.toggle()
        }

        fullscreenControl.fullscreen = true

        return fragment
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_AUTO_FULLSCREEN_DELAY){
            updateFullscreenControl()
        }
    }

    private fun updateFullscreenControl() {
        fullscreenControl.autoTurnOnDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
    }

}