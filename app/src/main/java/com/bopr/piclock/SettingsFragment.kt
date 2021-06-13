package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
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
        updateClockLayoutPreferenceView()
        updateTickAlwaysPreferenceView()
        updateAutoFullscreenPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_24_HOURS_FORMAT -> updateHourFormatPreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
            PREF_TICK_SOUND -> updateTickSoundPreferenceView()
            PREF_CLOCK_LAYOUT -> updateClockLayoutPreferenceView()
            PREF_TICK_SOUND_ALWAYS -> updateTickAlwaysPreferenceView()
            PREF_AUTO_FULLSCREEN_DELAY -> updateAutoFullscreenPreferenceView()
        }
    }

    private fun updateTickAlwaysPreferenceView() {
        requirePreference(PREF_TICK_SOUND_ALWAYS).apply {
            val text = if (settings.getBoolean(key)) {
                R.string.play_always
            } else {
                R.string.play_only_when_tapped
            }
            updateSummary(this, resources.getString(text))
        }
    }

    private fun updateHourFormatPreferenceView() {
        requirePreference(PREF_24_HOURS_FORMAT).apply {
            val sample = if (settings.getBoolean(key)) "18:00" else "6:00 pm"
            updateSummary(this, sample)
        }
    }

    private fun updateDateFormatPreferenceView() {
        val locale = Locale.getDefault()
        val patterns = resources.getStringArray(R.array.date_format_values)
        val date = Date()

        val entries = arrayOfNulls<String>(patterns.size)
        for (i in entries.indices) {
            entries[i] = SimpleDateFormat(patterns[i], locale).format(date)
        }

        (requirePreference(PREF_DATE_FORMAT) as ListPreference).apply {
            this.entries = entries
            updateSummary(this, SimpleDateFormat(settings.getString(key), locale).format(date))
        }
    }

    private fun updateClockLayoutPreferenceView() {
        (requirePreference(PREF_CLOCK_LAYOUT) as ListPreference).apply {
            val value = settings.getString(PREF_CLOCK_LAYOUT)
            updateSummary(this, entries[findIndexOfValue(value)])
        }
    }

    private fun updateTickSoundPreferenceView() {
        (requirePreference(PREF_TICK_SOUND) as ListPreference).apply {
            val value = settings.getString(PREF_TICK_SOUND)
            updateSummary(this, entries[findIndexOfValue(value)])
        }
    }

    private fun updateAutoFullscreenPreferenceView() {
        (requirePreference(PREF_AUTO_FULLSCREEN_DELAY) as ListPreference).apply {
            val value = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
            val summary = if (value > 0) {
                val index = findIndexOfValue(value.toString())
                getString(R.string.auto_fullscreen_summary, entries[index])
            } else {
                getString(R.string.auto_fullscreen_never_summary)
            }
            updateSummary(this, summary)
        }
    }

}
