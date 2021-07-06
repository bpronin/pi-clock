package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SeekBarPreference
import androidx.recyclerview.widget.LinearLayoutManager
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ABOUT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TOP_SETTING
import com.bopr.piclock.Settings.Companion.SHARED_PREFERENCES_NAME
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.*
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment
import java.util.*

/**
 * Application settings fragment.
 */
class SettingsFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    //todo: add: tick sound dialog with preview

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)

        updateAboutPreferenceView()
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
            PREF_AUTO_INACTIVATE_DELAY -> updateAutoInactivatePreferenceView()
            PREF_CONTENT_FLOAT_INTERVAL -> updateFloatIntervalPreferenceView()
            PREF_CONTENT_LAYOUT -> updateClockLayoutPreferenceView()
            PREF_CONTENT_SCALE -> updateScalePreferenceView()
            PREF_DATE_FORMAT -> updateDateFormatPreferenceView()
            PREF_MUTED_BRIGHTNESS -> updateMutedBrightnessPreferenceView()
            PREF_SECONDS_FORMAT -> updateSecondsFormatPreferenceView()
            PREF_TICK_SOUND -> updateTickSoundPreferenceView()
            PREF_TICK_RULES -> updateTickModePreferenceView()
            PREF_TIME_FORMAT -> updateTimeFormatPreferenceView()
            PREF_TIME_SEPARATORS_VISIBLE -> updateSeparatorsPreferenceViews()
            PREF_DIGITS_ANIMATION -> updateDigitsAnimationPreferenceViews()
        }
    }

    private fun updateAboutPreferenceView() {
        requirePreference(PREF_ABOUT).apply {
            val info = ReleaseInfo.get(requireContext())
            summary =
                getString(R.string.about_summary, info.versionName, info.buildNumber)
            onPreferenceClickListener = AboutPreferenceClickListener()
        }
    }

    private fun updateDigitsAnimationPreferenceViews() {
        (requirePreference(PREF_DIGITS_ANIMATION) as ListPreference).apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateSeparatorsPreferenceViews() {
        requirePreference(PREF_TIME_SEPARATORS_BLINKING).isEnabled =
            settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)
    }

    private fun updateFloatIntervalPreferenceView() {
        (requirePreference(PREF_CONTENT_FLOAT_INTERVAL) as ListPreference).apply {
            val value = settings.getLong(key)
            summary = when (value) {
                0L -> getString(R.string.clock_always_moves_along_screen)
                -1L -> getString(R.string.keep_clock_view)
                else -> getString(
                    R.string.clock_moves_along_screen,
                    entries[findIndexOfValue(value.toString())]
                )
            }
        }
    }

    private fun updateScalePreferenceView() {
        (requirePreference(PREF_CONTENT_SCALE) as SeekBarPreference).apply {
            min = MIN_SCALE
            max = MAX_SCALE
            value = settings.getInt(key)
            summary = getString(R.string.scale_summary, value)
        }
    }

    private fun updateTickModePreferenceView() {
        (requirePreference(PREF_TICK_RULES) as MultiSelectListPreference).apply {
            val titles = mutableListOf<String>()
            for (item in settings.getStringSet(key)) {
                titles.add(entries[entryValues.indexOf(item)].toString())
            }
            summary = if (titles.isNotEmpty())
                titles.sorted().joinToString(", ")
            else
                getString(R.string.never_tick)
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
            summary = entryNames[findIndexOfValue(settings.getString(key))]
        }
    }

    private fun updateTimeFormatPreferenceView() {
        (requirePreference(PREF_TIME_FORMAT) as ListPreference).apply {
            val value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.time_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateSecondsFormatPreferenceView() {
        (requirePreference(PREF_SECONDS_FORMAT) as ListPreference).apply {
            val value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.seconds_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateClockLayoutPreferenceView() {
        (requirePreference(PREF_CONTENT_LAYOUT) as ListPreference).apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateTickSoundPreferenceView() {
        (requirePreference(PREF_TICK_SOUND) as ListPreference).apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateAutoInactivatePreferenceView() {
        (requirePreference(PREF_AUTO_INACTIVATE_DELAY) as ListPreference).apply {
            val value = settings.getLong(key)
            summary = if (value > 0) {
                val index = findIndexOfValue(value.toString())
                getString(R.string.auto_inactivate_summary, entries[index])
            } else {
                getString(R.string.auto_inactivate_never_summary)
            }
        }
    }

    private fun updateMutedBrightnessPreferenceView() {
        (requirePreference(PREF_MUTED_BRIGHTNESS) as SeekBarPreference).apply {
            min = MIN_BRIGHTNESS
            max = MAX_BRIGHTNESS
            value = settings.getInt(key)
            summary = getString(R.string.muted_brightness_summary, value)
        }
    }

    private inner class AboutPreferenceClickListener : OnPreferenceClickListener {

        private var clicksCount: Int = 0
/*
        private var messageIndex: Int = 0
        private val messages = arrayListOf(
            "Wrong rhythm! You are definitely not me.",
            "Oops! Try again.",
            "Well I'll tell you. The rhythm is ta ta-ta ta.",
            "Almost. But not.",
            "OK. I'm just kidding. There is nothing here.",
            "Resetting messages counter...",
            "Wrong rhythm! You are definitely not me.",
            "And you are persistent!",
            "I give up. The secret code is 42. Use it ... somewhere."
        )
*/

        override fun onPreferenceClick(preference: Preference?): Boolean {
            if (++clicksCount == 4) {
                clicksCount = 0

                context?.run {
                    passwordBox("Enter some secret code") {
                        if (getString(R.string.debug_sha) == sha512(it)) {
                            startActivity(Intent(context, DebugActivity::class.java))
                        } else {
                            messageBox("Are you really trying to hack the clock app?")
                        }
                    }
                }
/*
                toast(messages[messageIndex++])

                if (messageIndex >= messages.size) {
                    messageIndex = 0

                    MediaPlayer.create(requireContext(), R.raw.tada).run {
                        setOnCompletionListener { release() }
                        start()
                    }
                }
*/

            }
            return true
        }
    }

}
