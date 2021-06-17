package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.util.sha512
import com.bopr.piclock.util.ui.BaseActivity

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

        handleStartupParams(intent)
    }

    override fun onDestroy() {
        fullscreenControl.destroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateFragment(): Fragment {
        return ClockFragment().apply {
            onReady = { active ->
                fullscreenControl.fullscreen = !active
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_FULLSCREEN_ENABLED) {
            fullscreenControl.enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }
    }

    private fun handleStartupParams(intent: Intent) {
        intent.getStringExtra("target")?.also { target ->
            when (target) {
                "settings" ->
                    startActivity(Intent(this, SettingsActivity::class.java))
                "debug" -> {
                    intent.getStringExtra("pwd")?.also { pwd ->
                        if (getString(R.string.debug_sha) == sha512(pwd)) {
                            startActivity(Intent(this, DebugActivity::class.java))
                        }
                    }
                }
                else ->
                    throw IllegalArgumentException("Invalid target: $target")
            }
        }
    }
}