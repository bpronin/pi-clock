package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
    }

    override fun onStart() {
        super.onStart()
        updateHourFormatPreferenceView()
        updateDateFormatPreferenceView()
        updateTickSoundPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_24_HOURS_FORMAT -> updateHourFormatPreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
            PREF_TICK_SOUND -> updateTickSoundPreferenceView()
        }
    }

    private fun updateHourFormatPreferenceView() {
        requirePreference(PREF_24_HOURS_FORMAT).apply {
            val sample = if (settings.getBoolean(key)) "18:00" else "6:00 pm"
            updateSummary(this, sample)
        }
    }

    private fun updateDateFormatPreferenceView() {
        val patterns = resources.getStringArray(R.array.date_format_values)
        val date = Date()
        val locale = Locale.getDefault()

        val entries = arrayOfNulls<String>(patterns.size)
        for (i in entries.indices) {
            entries[i] = SimpleDateFormat(patterns[i], locale).format(date)
        }

        (requirePreference(PREF_DATE_FORMAT) as ListPreference).apply {
            this.entries = entries
            updateSummary(this, SimpleDateFormat(settings.getString(key), locale).format(date))
        }
    }

    private fun updateTickSoundPreferenceView() {
        (requirePreference(PREF_TICK_SOUND) as ListPreference).apply {
            val value = settings.getString(PREF_TICK_SOUND)
            updateSummary(this, entries[findIndexOfValue(value)])
        }
    }

}
