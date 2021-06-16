package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MIN_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.SHARED_PREFERENCES_NAME
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

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
        updateScalePreferenceView()
        updateMinBrightnessPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_24_HOURS_FORMAT -> updateHourFormatPreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
            PREF_TICK_SOUND -> updateTickSoundPreferenceView()
            PREF_CLOCK_LAYOUT -> updateClockLayoutPreferenceView()
            PREF_TICK_SOUND_ALWAYS -> updateTickAlwaysPreferenceView()
            PREF_AUTO_INACTIVATE_DELAY -> updateAutoFullscreenPreferenceView()
            PREF_CLOCK_SCALE -> updateScalePreferenceView()
            PREF_MIN_BRIGHTNESS -> updateMinBrightnessPreferenceView()
        }
    }

    private fun updateScalePreferenceView() {
        requirePreference(PREF_CLOCK_SCALE).apply {
            summary = fixSummaryPercents(
                resources.getString(
                    R.string.scale_summary,
                    settings.getFloat(PREF_CLOCK_SCALE) * 100f
                )
            )
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
            entryNames[i] = when {
                pattern.isEmpty() ->
                    getString(R.string.do_not_show_date)
                pattern == SYSTEM_DEFAULT ->
                    getString(R.string.default_date_format, DEFAULT_DATE_FORMAT.format(date))
                else ->
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
        (requirePreference(PREF_AUTO_INACTIVATE_DELAY) as ListPreference).apply {
            val value = settings.getLong(PREF_AUTO_INACTIVATE_DELAY)
            summary = if (value > 0) {
                val index = findIndexOfValue(value.toString())
                getString(R.string.auto_fullscreen_summary, entries[index])
            } else {
                getString(R.string.auto_fullscreen_never_summary)
            }
        }
    }

    private fun updateMinBrightnessPreferenceView() {
        requirePreference(PREF_MIN_BRIGHTNESS).apply {
            val value = settings.getInt(PREF_MIN_BRIGHTNESS)
            summary = getString(R.string.min_brightness_summary, value)
        }
    }

    /** Single percent signs in entry names causes an exception */
    private fun fixSummaryPercents(s: String) = s.replace("%", "%%")
}
