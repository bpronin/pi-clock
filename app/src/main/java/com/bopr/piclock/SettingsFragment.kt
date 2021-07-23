package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bopr.piclock.AnalogClockControl.Companion.isAnalogClockBarsDateLayout
import com.bopr.piclock.AnalogClockControl.Companion.isAnalogClockLayout
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.DigitalClockControl.Companion.isDigitalClockLayout
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_HAND_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_HAND_MOVE_SMOOTH
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_STYLE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_SPLIT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_SPEED
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_HOURS_MINUTES_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECOND_HAND_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TOP_SETTING
import com.bopr.piclock.Settings.Companion.PREF_WEEK_START
import com.bopr.piclock.Settings.Companion.SHARED_PREFERENCES_NAME
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.*
import com.bopr.piclock.util.ui.preference.CustomPreferenceFragment
import com.bopr.piclock.util.ui.preference.IntListPreference
import java.text.DateFormatSymbols
import java.util.*

/**
 * Application settings fragment.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class SettingsFragment : CustomPreferenceFragment(), OnSharedPreferenceChangeListener {

    private val settings by lazy { Settings(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        settings.addListener(this)
    }

    override fun onDestroy() {
        settings.removeListener(this)
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)
        updateLayoutPreferences()
    }

    override fun onStart() {
        super.onStart()
        updateAboutView()
        refreshPreferenceViews(preferenceScreen)
        /* restore last scroll position */
        listView.scrollToPosition(settings.getInt(PREF_TOP_SETTING, 0))
    }

    override fun onStop() {
        (listView.layoutManager as LinearLayoutManager).apply {
            /* save last scroll position */
            settings.update { putInt(PREF_TOP_SETTING, findFirstCompletelyVisibleItemPosition()) }
        }

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        /* NOTE!: do not use preferenceView's value field in updateXXXView methods to check
            current setting state. The value field changes later than we need */
        when (key) {
            PREF_CONTENT_LAYOUT -> {
                updateLayoutView()
                updateStyleViewEntries()
                updateLayoutPreferences()
            }
            PREF_ANIMATION_ON -> updateAnimationOnView()
            PREF_AUTO_INACTIVATE_DELAY -> updateAutoInactivateView()
            PREF_CLOCK_HAND_ANIMATION -> updateClockHandAnimationView()
            PREF_CLOCK_HAND_MOVE_SMOOTH -> updateMoveHandSmoothView()
            PREF_CONTENT_FLOAT_INTERVAL -> updateFloatIntervalView()
            PREF_CONTENT_SCALE -> updateScaleView()
            PREF_CONTENT_STYLE -> updateStyleView()
            PREF_DATE_FORMAT -> updateDateFormatView()
            PREF_DIGITS_ANIMATION -> updateDigitsAnimationView()
            PREF_DIGITS_SPLIT_ANIMATION -> updateDigitsSplitAnimationView()
            PREF_FLOAT_ANIMATION -> updateFloatAnimationView()
            PREF_FLOAT_SPEED -> updateFloatSpeedView()
            PREF_FULLSCREEN_ENABLED -> updateFullscreenView()
            PREF_GESTURES_ENABLED -> updateGesturesView()
            PREF_HOURS_MINUTES_FORMAT -> updateHoursMinutesFormatView()
            PREF_MUTED_BRIGHTNESS -> updateMutedBrightnessView()
            PREF_SECONDS_FORMAT -> updateSecondsFormatView()
            PREF_SECOND_HAND_VISIBLE -> updateSecondHandView()
            PREF_TICK_RULES -> updateTickRulesView()
            PREF_TICK_SOUND -> updateTickSoundView()
            PREF_TIME_SEPARATORS_BLINKING -> updateTimeSeparatorsBlinkingView()
            PREF_TIME_SEPARATORS_VISIBLE -> updateTimeSeparatorsVisibleView()
            PREF_WEEK_START -> updateWeekStartView()
        }
    }

    /** Forces update preference views recursively */
    private fun refreshPreferenceViews(root: PreferenceGroup, vararg exclude: String) {
        root.forEachChildRecursively {
            it.key?.apply {
                if (this !in exclude) {
                    onSharedPreferenceChanged(settings, this)
                }
            }
        }
    }

    private fun isDigitalLayout(): Boolean {
       return isDigitalClockLayout(settings.getString(PREF_CONTENT_LAYOUT))
    }

    private fun isAnalogLayout(): Boolean {
       return isAnalogClockLayout(settings.getString(PREF_CONTENT_LAYOUT))
    }

    private fun isBarsDateLayout(): Boolean {
       return isAnalogClockBarsDateLayout(settings.getString(PREF_CONTENT_LAYOUT))
    }

    private fun updateLayoutPreferences() {
        updateTimeSeparatorsVisibleView()
        updateDigitsAnimationView()
        updateDigitsSplitAnimationView()
        updateSecondsFormatView()
        updateHoursMinutesFormatView()
        updateDateFormatView()
        updateWeekStartView()
        requirePreference<Preference>(PREF_TIME_SEPARATORS).isVisible = isDigitalLayout()
        requirePreference<Preference>(PREF_HANDS).isVisible = isAnalogLayout()
    }

    private fun updateAboutView() {
        requirePreference<Preference>(PREF_ABOUT_APP).apply {
            val info = ReleaseInfo.get(requireContext())
            summary = getString(R.string.about_summary, info.versionName, info.buildNumber)
            onPreferenceClickListener = AboutPreferenceClickListener()
        }
    }

    private fun updateFloatIntervalView() {
        requirePreference<ListPreference>(PREF_CONTENT_FLOAT_INTERVAL).apply {
            val value = settings.getLong(key)
            summary = when (value) {
                0L -> getString(R.string.clock_always_moves_along_screen)
                -1L -> getString(R.string.clock_in_center)
                else -> getString(
                    R.string.clock_moves_along_screen,
                    entries[findIndexOfValue(value.toString())]
                )
            }
            requirePreference<Preference>(PREF_FLOAT_ANIMATION).isEnabled = value != -1L
            requirePreference<Preference>(PREF_FLOAT_SPEED).isEnabled = value != -1L
        }
    }

    private fun updateFloatAnimationView() {
        requirePreference<ListPreference>(PREF_FLOAT_ANIMATION).apply {
            value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateFloatSpeedView() {
        requirePreference<IntListPreference>(PREF_FLOAT_SPEED).apply {
            val value = settings.getInt(key)
            val ix = findIndexOfValue(value.toString())
            summary = if (ix >= 0) entries[ix] else null
        }
    }

    private fun updateScaleView() {
        requirePreference<SeekBarPreference>(PREF_CONTENT_SCALE).apply {
            value = settings.getInt(key)
            min = MIN_SCALE
            max = MAX_SCALE
            summary = getString(R.string.scale_summary, value)
        }
    }

    private fun updateTickRulesView() {
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
                    getString(R.string.when_in, titles.sorted().joinToString(", ").lowercase())
                }
            }

        }
    }

    private fun updateMutedBrightnessView() {
        requirePreference<SeekBarPreference>(PREF_MUTED_BRIGHTNESS).apply {
            value = settings.getInt(key)
            min = MIN_BRIGHTNESS
            max = MAX_BRIGHTNESS
            summary = getString(R.string.muted_brightness_summary, value)
        }
    }

    private fun updateTickSoundView() {
        requirePreference<Preference>(PREF_TICK_SOUND).apply {
            val value = settings.getString(key)
            val index = getStringArray(R.array.tick_sound_values).indexOf(value)
            summary = getStringArray(R.array.tick_sound_titles)[index]
        }
    }

    private fun updateLayoutView() {
        requirePreference<ListPreference>(PREF_CONTENT_LAYOUT).apply {
            value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateStyleView() {
        requirePreference<ListPreference>(PREF_CONTENT_STYLE).apply {
            entries.apply {
                value = settings.getString(key)
                summary = entries[findIndexOfValue(value)]
                isEnabled = entryValues.size > 1
            }
        }
    }

    private fun updateStyleViewEntries() {
        val layoutName = settings.getString(PREF_CONTENT_LAYOUT)

        requireContext().apply {
            val layoutResId = requireResId("layout", layoutName)
            val valuesResId = getStyleValuesResId(layoutResId)
            requirePreference<ListPreference>(PREF_CONTENT_STYLE).apply {
                if (valuesResId != 0) {
                    setEntryValues(valuesResId)
                    setEntries(getStyleTitlesResId(layoutResId))
                } else {
                    entryValues = null
                    entries = null
                }
            }
        }

        /* validate selected style */
        getLayoutStyles(layoutName).apply {
            if (contains(settings.getString(PREF_CONTENT_STYLE, null))) {
                updateStyleView()
            } else {
                /* setting update will trigger updateStyleView */
                settings.update { putString(PREF_CONTENT_STYLE, get(0)) }
            }
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
                    R.string.disabled_power_save
            )
        }
    }

    private fun updateHoursMinutesFormatView() {
        findPreference<ListPreference>(PREF_HOURS_MINUTES_FORMAT)?.apply {
            isVisible = isDigitalLayout()
            value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.hours_minutes_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateSecondsFormatView() {
        findPreference<ListPreference>(PREF_SECONDS_FORMAT)?.apply {
            isVisible = isDigitalLayout()
            value = settings.getString(key)
            val ix = findIndexOfValue(value)
            val hint = getStringArray(R.array.seconds_format_hints)[ix]
            summary = "${entries[ix]} $hint"
        }
    }

    private fun updateDateFormatView() {
        findPreference<ListPreference>(PREF_DATE_FORMAT)?.apply {
            val date = Date()
            val patterns = getStringArray(R.array.date_format_values)
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
            isVisible = !isBarsDateLayout()
            entries = entryNames
            summary = entryNames[findIndexOfValue(settings.getString(key))]
        }
    }

    private fun updateDigitsAnimationView() {
        findPreference<ListPreference>(PREF_DIGITS_ANIMATION)?.apply {
            isVisible = isDigitalLayout()
            value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateDigitsSplitAnimationView() {
        requirePreference<SwitchPreferenceCompat>(PREF_DIGITS_SPLIT_ANIMATION).apply {
            isVisible = isDigitalLayout()
            val value = settings.getBoolean(key)
            summary = getString(if (value) R.string.enabled else R.string.disabled)
        }
    }

    private fun updateWeekStartView() {
        requirePreference<ListPreference>(PREF_WEEK_START).apply {
            isVisible = isBarsDateLayout()
            entries = arrayOfNulls<String>(7)
            entryValues = arrayOfNulls<String>(7)
            val weekdays = DateFormatSymbols.getInstance().weekdays
            for (i in 1..7) {
                entryValues[i - 1] = i.toString()
                entries[i - 1] = weekdays[i]
            }
            value = settings.getInt(key).toString()
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateSecondHandView() {
        requirePreference<SwitchPreferenceCompat>(PREF_SECOND_HAND_VISIBLE).apply {
            summary = getString(if (settings.getBoolean(key)) R.string.visible else R.string.hidden)
        }
    }

    private fun updateClockHandAnimationView() {
        requirePreference<ListPreference>(PREF_CLOCK_HAND_ANIMATION).apply {
            value = settings.getString(key)
            summary = entries[findIndexOfValue(value)]
        }
    }

    private fun updateMoveHandSmoothView() {
        requirePreference<SwitchPreferenceCompat>(PREF_CLOCK_HAND_MOVE_SMOOTH).apply {
            summary = getString(
                if (settings.getBoolean(key))
                    R.string.move_hands_smooth
                else
                    R.string.move_hands_discretely
            )
        }
    }

    private fun updateTimeSeparatorsVisibleView() {
        requirePreference<SwitchPreferenceCompat>(PREF_TIME_SEPARATORS_VISIBLE).apply {
            isVisible = isDigitalLayout()
            val value = settings.getBoolean(key)
            summary = getString(if (value) R.string.visible else R.string.hidden)
            requirePreference<Preference>(PREF_TIME_SEPARATORS_BLINKING).isEnabled = value
        }
    }

    private fun updateTimeSeparatorsBlinkingView() {
        requirePreference<SwitchPreferenceCompat>(PREF_TIME_SEPARATORS_BLINKING).apply {
            summary = getString(
                if (settings.getBoolean(key)) R.string.enabled else R.string.disabled
            )
        }
    }

    private inner class AboutPreferenceClickListener : OnPreferenceClickListener {

        private var clicksCount = 0

        override fun onPreferenceClick(preference: Preference?): Boolean {
            if (++clicksCount == 4) {
                clicksCount = 0
                passwordBox("Enter some secret code") {
                    if (getString(R.string.developer_sha) == sha512(it)) {
                        startActivity(Intent(context, DebugActivity::class.java))
                    } else {
                        messageBox("Are you really trying to hack a clock app? :)")
                    }
                }
            }
            return true
        }
    }

    companion object {

        /* dummy settings to refer to preference views */
        private const val PREF_ABOUT_APP = "about_app"
        private const val PREF_HANDS = "hands"
        private const val PREF_TIME_SEPARATORS = "time_separators"
    }
}
