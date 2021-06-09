package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.util.clockDateFormat
import java.util.*

class SettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
    }

    override fun onStart() {
        super.onStart()
        updateHourFormatPreferenceView()
        updateDateFormatPreferenceView()
        updateShowSecondsPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_SECONDS_VISIBLE -> updateShowSecondsPreferenceView()
            PREF_24_HOURS_FORMAT -> updateHourFormatPreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
        }
    }

    private fun updateHourFormatPreferenceView() {
        requirePreference(PREF_24_HOURS_FORMAT).apply {
            val sample = if (settings.getBoolean(key)) "18:00" else "6:00 pm"
            updateSummary(this, sample)
        }
    }

    private fun updateDateFormatPreferenceView() {
        requirePreference(PREF_DATE_FORMAT).apply {
            val format = clockDateFormat(settings.getString(key))
            updateSummary(this, format.format(Date()))
        }
    }

    private fun updateShowSecondsPreferenceView() {
        requirePreference(PREF_SECONDS_VISIBLE).apply {
            val sample = if (settings.getBoolean(key)) "18:00:00" else "18:00"
            updateSummary(this, sample)
        }
    }

}
