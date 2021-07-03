package com.bopr.piclock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.recyclerview.widget.LinearLayoutManager
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_INACTIVE_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TOP_SETTING
import com.bopr.piclock.Settings.Companion.SHARED_PREFERENCES_NAME
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.getStringArray
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment
import java.util.*

/**
 * Visual editor fragment.
 */
//todo: make simple fragment
class EditorFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings

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
        addPreferencesFromResource(R.xml.pref_editor)
    }

    override fun onStart() {
        super.onStart()

        /* force update preference views at startup */
        for (key in settings.all.keys) {
            onSharedPreferenceChanged(settings, key)
        }

        /* restore last scroll position */
        listView.scrollToPosition(settings.getInt(PREF_TOP_SETTING, 0))
    }

    override fun onStop() {
        /* save last scroll position */
        (listView.layoutManager as LinearLayoutManager).apply {
            settings.update { putInt(PREF_TOP_SETTING, findFirstCompletelyVisibleItemPosition()) }
        }

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_CONTENT_LAYOUT -> updateClockLayoutPreferenceView()
            PREF_CONTENT_SCALE -> updateScalePreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
            PREF_INACTIVE_BRIGHTNESS -> updateMinBrightnessPreferenceView()
            PREF_SECONDS_FORMAT -> updateSecondsFormatPreferenceView()
            PREF_TIME_FORMAT -> updateTimeFormatPreferenceView()
            PREF_TIME_SEPARATORS_VISIBLE -> updateSeparatorsPreferenceViews()
            PREF_DIGITS_ANIMATION -> updateDigitsAnimationPreferenceViews()
        }
    }

    private fun updateDigitsAnimationPreferenceViews() {
        (requirePreference(PREF_DIGITS_ANIMATION) as ListPreference).apply {
            val value = settings.getString(PREF_DIGITS_ANIMATION)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateSeparatorsPreferenceViews() {
        requirePreference(PREF_TIME_SEPARATORS_BLINKING).isEnabled =
            settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)
    }

    private fun updateScalePreferenceView() {
        requirePreference(PREF_CONTENT_SCALE).apply {
            val value = settings.getFloat(PREF_CONTENT_SCALE) * 100f
            /* Single percent sign in summary of causes an exception here. */
            summary = getString(R.string.scale_summary, value).replace("%", "%%")
        }
    }

    private fun updateDateFormatPreferenceView() {
        val patterns = getStringArray(R.array.date_format_values)
        val date = Date()

        val entryNames = arrayOfNulls<String>(patterns.size)
        for (i in entryNames.indices) {
            val pattern = patterns[i]
            entryNames[i] = when {
                pattern.isEmpty() ->
                    getString(R.string.do_not_show)
                pattern == SYSTEM_DEFAULT ->
                    getString(R.string.default_date_format, DEFAULT_DATE_FORMAT.format(date))
                else ->
                    defaultDatetimeFormat(pattern).format(date)
            }
        }

        (requirePreference(PREF_DATE_FORMAT) as ListPreference).apply {
            entries = entryNames
            summary = entryNames[findIndexOfValue(settings.getString(PREF_DATE_FORMAT))]
        }
    }

    private fun updateTimeFormatPreferenceView() {
        (requirePreference(PREF_TIME_FORMAT) as ListPreference).apply {
            val value = settings.getString(PREF_TIME_FORMAT)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.time_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateSecondsFormatPreferenceView() {
        (requirePreference(PREF_SECONDS_FORMAT) as ListPreference).apply {
            val value = settings.getString(PREF_SECONDS_FORMAT)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.seconds_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateClockLayoutPreferenceView() {
        (requirePreference(PREF_CONTENT_LAYOUT) as ListPreference).apply {
            val value = settings.getString(PREF_CONTENT_LAYOUT)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateMinBrightnessPreferenceView() {
        requirePreference(PREF_INACTIVE_BRIGHTNESS).apply {
            val value = settings.getInt(PREF_INACTIVE_BRIGHTNESS)
            summary = getString(R.string.min_brightness_summary, value)
        }
    }

}
