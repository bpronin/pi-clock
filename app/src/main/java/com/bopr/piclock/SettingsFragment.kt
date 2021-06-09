package com.bopr.piclock

import android.os.Bundle

class SettingsFragment: BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
    }

}
