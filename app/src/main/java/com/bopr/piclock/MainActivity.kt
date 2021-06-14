package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.ui.BaseActivity

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

        settings = Settings(this).apply {
            validate()
            registerOnSharedPreferenceChangeListener(this@MainActivity)
        }

        fullscreenControl = FullscreenSupport(window).apply {
            enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }
    }

    override fun onDestroy() {
        fullscreenControl.destroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateFragment(): Fragment {
        return ClockFragment().apply {
            onActivate = { active ->
                fullscreenControl.fullscreen = !active
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_FULLSCREEN_ENABLED) {
            fullscreenControl.enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }
    }

}