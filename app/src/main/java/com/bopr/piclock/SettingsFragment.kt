package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.DigitalClockControl.Companion.isDigitalClockLayout
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TOP_SETTING
import com.bopr.piclock.Settings.Companion.SHARED_PREFERENCES_NAME
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.*
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment
import java.util.*

/**
 * Application settings fragment.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SettingsFragment : CustomPreferenceFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val settings by lazy { Settings(requireContext()) }
    private var layoutSpecificPrefResId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)
        loadLayoutSpecificPreferences(settings.getString(PREF_CONTENT_LAYOUT))
    }

    override fun onStart() {
        super.onStart()
        updateAboutView()
        refreshPreferences()
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
            PREF_AUTO_INACTIVATE_DELAY -> updateAutoInactivateView()
            PREF_CONTENT_FLOAT_INTERVAL -> updateFloatIntervalView()
            PREF_CONTENT_LAYOUT -> updateLayoutView()
            PREF_CONTENT_SCALE -> updateScaleView()
            PREF_DATE_FORMAT -> updateDateFormatView()
            PREF_MUTED_BRIGHTNESS -> updateMutedBrightnessView()
            PREF_SECONDS_FORMAT -> updateSecondsFormatView()
            PREF_TICK_SOUND -> updateTickSoundView()
            PREF_TICK_RULES -> updateTickModeView()
            PREF_TIME_FORMAT -> updateTimeFormatView()
            PREF_DIGITS_ANIMATION -> updateDigitsAnimationView()
            PREF_GESTURES_ENABLED -> updateGesturesView()
            PREF_FULLSCREEN_ENABLED -> updateFullscreenView()
            PREF_ANIMATION_ON -> updateAnimationOnView()
            PREF_FLOAT_ANIMATION -> updateFloatAnimationView()
        }
    }

    /** Forces update preference views */
    private fun refreshPreferences() {
        for (key in settings.all.keys) {
            onSharedPreferenceChanged(settings, key)
        }
    }

    private fun loadLayoutSpecificPreferences(layout: String) {
        val contentPrefResId = when {
            isDigitalClockLayout(layout) -> R.xml.pref_digital_layout
            else -> throw IllegalArgumentException("No preferences xml for layout:$layout")
        }

        if (contentPrefResId == layoutSpecificPrefResId) return
        layoutSpecificPrefResId = contentPrefResId

        val layoutPref = requirePreference<Preference>(PREF_CONTENT_LAYOUT).apply {
            order = -1 /* keep it first */
        }
        val holderPref = requirePreference<PreferenceGroup>("layout_specific_holder").apply {
            removeAll()
            addPreference(layoutPref)
        }

        addPreferencesFromResource(contentPrefResId)
        val contentPref = requirePreference<PreferenceGroup>("layout_specific_content")
        preferenceScreen.removePreference(contentPref) /* we do not need it anymore */
        while (contentPref.isNotEmpty()) {
            contentPref[0].apply {
                contentPref.removePreference(this)
                holderPref.addPreference(this)
            }
        }
    }

    private fun updateAboutView() {
        requirePreference<Preference>("about_app").apply {
            val info = ReleaseInfo.get(requireContext())
            summary =
                getString(R.string.about_summary, info.versionName, info.buildNumber)
            onPreferenceClickListener = AboutPreferenceClickListener()
        }
    }

    private fun updateFloatAnimationView() {
        requirePreference<ListPreference>(PREF_FLOAT_ANIMATION).apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateFloatIntervalView() {
        requirePreference<ListPreference>(PREF_CONTENT_FLOAT_INTERVAL).apply {
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

    private fun updateScaleView() {
        requirePreference<SeekBarPreference>(PREF_CONTENT_SCALE).apply {
            min = MIN_SCALE
            max = MAX_SCALE
            value = settings.getInt(key)
            summary = getString(R.string.scale_summary, value)
        }
    }

    private fun updateTickModeView() {
        requirePreference<MultiSelectListPreference>(PREF_TICK_RULES).apply {
            val items = settings.getStringSet(key)

            summary = when (items.size) {
                entries.size ->
                    getString(R.string.tick_always)
                0 ->
                    getString(R.string.tick_never)
                else -> {
                    val titles = mutableListOf<String>()
                    for (item in items) {
                        titles.add(entries[entryValues.indexOf(item)].toString())
                    }
                    titles.sorted().joinToString(", ")
                }
            }

        }
    }

    private fun updateLayoutView() {
        requirePreference<ListPreference>(PREF_CONTENT_LAYOUT).apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
            setOnPreferenceChangeListener { _, newValue ->
                loadLayoutSpecificPreferences(newValue as String)
                refreshPreferences()
                true
            }
        }
    }

    private fun updateTickSoundView() {
        requirePreference<Preference>(PREF_TICK_SOUND).apply {
            val value = settings.getString(key)
            val index = context.getStringArray(R.array.tick_sound_values).indexOf(value)
            summary = context.getStringArray(R.array.tick_sound_names)[index]
        }
    }

    private fun updateAutoInactivateView() {
        requirePreference<ListPreference>(PREF_AUTO_INACTIVATE_DELAY).apply {
            val value = settings.getLong(key)
            summary = if (value > 0) {
                val index = findIndexOfValue(value.toString())
                getString(R.string.auto_inactivate_summary, entries[index])
            } else {
                getString(R.string.auto_inactivate_never_summary)
            }
        }
    }

    private fun updateMutedBrightnessView() {
        requirePreference<SeekBarPreference>(PREF_MUTED_BRIGHTNESS).apply {
            min = MIN_BRIGHTNESS
            max = MAX_BRIGHTNESS
            value = settings.getInt(key)
            summary = getString(R.string.muted_brightness_summary, value)
        }
    }

    private fun updateFullscreenView() {
        requirePreference<SwitchPreferenceCompat>(PREF_FULLSCREEN_ENABLED).apply {
            summary = getString(
                if (settings.getBoolean(key))
                    R.string.fullscreen_enabled_summary
                else
                    R.string.fullscreen_disabled_summary
            )
        }
    }

    private fun updateGesturesView() {
        requirePreference<SwitchPreferenceCompat>(PREF_GESTURES_ENABLED).apply {
            summary = getString(
                if (settings.getBoolean(key))
                    R.string.gestures_enabled_summary
                else
                    R.string.gestures_disabled_summary
            )
        }
    }

    private fun updateAnimationOnView() {
        requirePreference<SwitchPreferenceCompat>(PREF_ANIMATION_ON).apply {
            summary = getString(
                if (settings.getBoolean(key))
                    R.string.enabled
                else
                    R.string.disabled
            )
        }
    }

    private fun updateTimeFormatView() {
        findPreference<ListPreference>(PREF_TIME_FORMAT)?.apply {
            val value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = context.getStringArray(R.array.time_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateSecondsFormatView() {
        findPreference<ListPreference>(PREF_SECONDS_FORMAT)?.apply {
            val value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = context.getStringArray(R.array.seconds_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateDateFormatView() {
        findPreference<ListPreference>(PREF_DATE_FORMAT)?.apply {

            val date = Date()
            val patterns = context.getStringArray(R.array.date_format_values)
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

            entries = entryNames
            summary = entryNames[findIndexOfValue(settings.getString(key))]
        }
    }

    private fun updateDigitsAnimationView() {
        findPreference<ListPreference>(PREF_DIGITS_ANIMATION)?.apply {
            val value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
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
                        if (getString(R.string.developer_sha) == sha512(it)) {
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
