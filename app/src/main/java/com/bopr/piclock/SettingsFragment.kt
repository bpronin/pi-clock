package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.ui.BasePreferenceFragment
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
        updateClockBrightnessPreferenceView()
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
            PREF_CLOCK_BRIGHTNESS -> updateClockBrightnessPreferenceView()
        }
    }

    private fun updateTickAlwaysPreferenceView() {
        requirePreference(PREF_TICK_SOUND_ALWAYS).apply {
            summary = resources.getString(
                if (settings.getBoolean(key)) {
                    R.string.play_always
                } else {
                    R.string.play_only_when_tapped
                }
            )
        }
    }

    private fun updateHourFormatPreferenceView() {
        requirePreference(PREF_24_HOURS_FORMAT).apply {
            summary = SimpleDateFormat(
                if (settings.getBoolean(key)) "HH:mm" else "h:mm a",
                Locale.getDefault()
            ).format(Date())
        }
    }

    private fun updateDateFormatPreferenceView() {
        val patterns = resources.getStringArray(R.array.date_format_values)
        val date = Date()
        val locale = Locale.getDefault()

        val entryNames = arrayOfNulls<String>(patterns.size)
        for (i in entryNames.indices) {
            val pattern = patterns[i]
            entryNames[i] = if (pattern.isEmpty()) {
                getString(R.string.do_not_show_date)
            } else {
                SimpleDateFormat(pattern, locale).format(date)
            }
        }

        (requirePreference(PREF_DATE_FORMAT) as ListPreference).apply {
            entries = entryNames
            summary = entryNames[findIndexOfValue(settings.getString(key))]
        }
    }

    private fun updateClockLayoutPreferenceView() {
        (requirePreference(PREF_CLOCK_LAYOUT) as ListPreference).apply {
            val value = settings.getString(PREF_CLOCK_LAYOUT)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateTickSoundPreferenceView() {
        (requirePreference(PREF_TICK_SOUND) as ListPreference).apply {
            val value = settings.getString(PREF_TICK_SOUND)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateAutoFullscreenPreferenceView() {
        (requirePreference(PREF_AUTO_FULLSCREEN_DELAY) as ListPreference).apply {
            val value = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
            summary = if (value > 0) {
                val index = findIndexOfValue(value.toString())
                getString(R.string.auto_fullscreen_summary, entries[index])
            } else {
                getString(R.string.auto_fullscreen_never_summary)
            }
        }
    }

    private fun updateClockBrightnessPreferenceView() {
        (requirePreference(PREF_CLOCK_BRIGHTNESS) as ListPreference).apply {
            val value = settings.getInt(PREF_CLOCK_BRIGHTNESS).toString()
            /* NOTE: single percent signs in entry names causes an exception */
            summary = entries[findIndexOfValue(value)].toString().replace("%", "%%")
        }
    }
}
