package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment

/**
 * Debug actions fragment.
 *
 *  @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var settings: Settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /* do not use fragment's context here. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        preferenceScreen = preferenceManager.createPreferenceScreen(preferenceManager.context)

        addCategory(
            "Other",
            createPreference("Crash", this::onCrash)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        /* do something with settings */
    }

    private fun addCategory(
        caption: String,
        vararg preferences: Preference
    ) {
        PreferenceCategory(requireContext()).apply {
            title = caption
            preferenceScreen.addPreference(this)
            for (preference in preferences) {
                addPreference(preference)
            }
        }
    }

    private fun createPreference(
        caption: String,
        onClick: () -> Unit
    ): Preference {
        return Preference(requireContext()).apply {
            title = caption
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                onClick()
                true
            }
        }
    }

    private fun onCrash() {
        throw RuntimeException("Test crash")
    }

}
