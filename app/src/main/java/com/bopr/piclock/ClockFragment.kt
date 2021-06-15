package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MIN_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.ui.BaseFragment
import com.bopr.piclock.util.getResId
import com.bopr.piclock.util.requireViewByIdCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    /** Logger tag. */
    private val _tag = "ClockFragment"

    private lateinit var contentContainer: ViewGroup
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var hoursView: TextView
    private lateinit var minutesView: TextView
    private lateinit var secondsView: TextView
    private lateinit var dateView: TextView
    private lateinit var timeSeparator: TextView
    private lateinit var secondsSeparator: TextView
    private lateinit var amPmMarkerView: TextView

    private lateinit var settings: Settings
    private lateinit var amPmFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var hoursFormat: DateFormat
    private lateinit var dateFormat: DateFormat
    private lateinit var tickPlayer: TickPlayer
    private lateinit var animations: ClockFragmentAnimations
    private lateinit var scaleControl: ClockFragmentScaleControl

    private val locale = Locale.getDefault()
    private val handler = Handler(Looper.getMainLooper())

    private val timerTask = object : Runnable {

        override fun run() {
            onTimer()
            handler.postDelayed(this, 1000)
        }
    }

    private val autoDeactivateTask = Runnable {
        Log.d(_tag, "Auto deactivate")
        setActive(value = false, animate = true)
    }

    private var active = false
    private var ready = false

    var onReady: (active: Boolean) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        amPmFormat = SimpleDateFormat("a", locale)
        minutesFormat = SimpleDateFormat("mm", locale)
        secondsFormat = SimpleDateFormat("ss", locale)
        tickPlayer = TickPlayer(requireContext())
        animations = ClockFragmentAnimations()

        settings = Settings(requireContext()).apply {
            tickPlayer.soundName = getString(PREF_TICK_SOUND, null)

            registerOnSharedPreferenceChangeListener(this@ClockFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup

        view.setOnClickListener {
            setActive(!active, true)
        }

        settingsButton = view.requireViewByIdCompat(R.id.settings_button)
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        contentContainer = view.findViewById(R.id.content_container)

        scaleControl = ClockFragmentScaleControl(requireContext(), view, contentContainer).apply {
            factor = settings.getFloat(PREF_CLOCK_SCALE)
            onEnd = { factor ->
                settings.update { putFloat(PREF_CLOCK_SCALE, factor) }
            }
        }

        createContentView()
        setActive(savedState?.getBoolean("active") ?: false, false)

        return view
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.putBoolean("active", active)
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTask)
        animations.stop()
        tickPlayer.stop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        settings.apply {
            when (key) {
                PREF_CLOCK_LAYOUT ->
                    createContentView()
                PREF_24_HOURS_FORMAT ->
                    updateHoursView()
                PREF_SECONDS_VISIBLE ->
                    updateSecondsView()
                PREF_TIME_SEPARATOR_BLINKING ->
                    updateTimeSeparatorView()
                PREF_DATE_FORMAT ->
                    updateDateView()
                PREF_AUTO_FULLSCREEN_DELAY ->
                    scheduleAutoDeactivate()
                PREF_TICK_SOUND ->
                    tickPlayer.soundName = getString(PREF_TICK_SOUND, null)
                PREF_MIN_BRIGHTNESS ->
                    updateMinBrightness()
                PREF_CLOCK_SCALE -> {
                    scaleControl.factor = settings.getFloat(PREF_CLOCK_SCALE)
                }
            }
        }
    }

    private fun updateMinBrightness() {
        contentContainer.alpha = if (active) 1f else minTextBrightness()
    }

    private fun onTimer() {
        val time = Date()

        hoursView.text = hoursFormat.format(time)
        minutesView.text = minutesFormat.format(time)
        secondsView.text = secondsFormat.format(time)
        amPmMarkerView.text = amPmFormat.format(time)
        dateView.text = dateFormat.format(time)

        blinkTimeSeparator(time)
        playTickSound()
    }

    private fun setActive(value: Boolean, animate: Boolean) {
        Log.d(_tag, "Activating: $value")

        cancelAutoDeactivate()
        active = value
        ready = false

        updateActiveControls(animate) {
            Log.d(_tag, "Activate complete: $active")

            ready = true
            scheduleAutoDeactivate()
            onReady(active)
        }
    }

    private fun updateActiveControls(animate: Boolean, onComplete: () -> Unit) {
        val wantControlVolume = !settings.getBoolean(PREF_TICK_SOUND_ALWAYS)
        val minBrightness = minTextBrightness()

        if (active) {
            if (animate) {
                animations.showFab(settingsButton)
                animations.fadeInContent(contentContainer, minBrightness,
                    onStart = { animator ->
                        if (wantControlVolume) {
                            tickPlayer.fadeVolume(0f, 1f, animator.duration)
                        }
                        onComplete()
                    },
                    onEnd = {
                        updateMinBrightness()
                    })
            } else {
                settingsButton.visibility = VISIBLE
                updateMinBrightness()
                onComplete()
            }
        } else {
            if (animate) {
                animations.hideFab(settingsButton)
                animations.fadeOutContent(contentContainer, minBrightness,
                    onStart = { animator ->
                        if (wantControlVolume) {
                            tickPlayer.fadeVolume(1f, 0f, animator.duration)
                        }
                    },
                    onEnd = {
                        updateMinBrightness()
                        onComplete()
                    })
            } else {
                settingsButton.visibility = INVISIBLE
                updateMinBrightness()
                onComplete()
            }
        }
    }

    private fun createContentView() {
        Log.d(_tag, "Creating content")

        contentContainer.apply {
            removeAllViews()
            val resName = settings.getString(PREF_CLOCK_LAYOUT)
            val resId = requireContext().getResId("layout", resName)

            addView(layoutInflater.inflate(resId, this, false).apply {
                hoursView = requireViewByIdCompat(R.id.hours_view)
                minutesView = requireViewByIdCompat(R.id.minutes_view)
                secondsView = requireViewByIdCompat(R.id.seconds_view)
                amPmMarkerView = requireViewByIdCompat(R.id.am_pm_marker_view)
                dateView = requireViewByIdCompat(R.id.date_view)
                timeSeparator = requireViewByIdCompat(R.id.time_separator)
                secondsSeparator = requireViewByIdCompat(R.id.seconds_separator)
            })
        }

        updateHoursView()
        updateSecondsView()
        updateDateView()
        updateTimeSeparatorView()
        updateMinBrightness()
    }

    private fun updateHoursView() {
        if (settings.getBoolean(PREF_24_HOURS_FORMAT)) {
            hoursFormat = SimpleDateFormat("HH", locale)
            amPmMarkerView.visibility = GONE
        } else {
            hoursFormat = SimpleDateFormat("h", locale)
            amPmMarkerView.visibility = VISIBLE
        }
    }

    private fun updateSecondsView() {
        if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
            secondsView.visibility = VISIBLE
            secondsSeparator.visibility = VISIBLE
        } else {
            secondsView.visibility = GONE
            secondsSeparator.visibility = GONE
        }
    }

    private fun updateDateView() {
        val pattern = settings.getString(PREF_DATE_FORMAT)
        if (pattern.isNotEmpty()) {
            dateFormat = if (pattern == SYSTEM_DEFAULT) {
                DEFAULT_DATE_FORMAT
            } else {
                SimpleDateFormat(pattern, locale)
            }
            dateView.visibility = VISIBLE
        } else {
            dateView.visibility = GONE
        }
    }

    private fun updateTimeSeparatorView() {
        if (!settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            timeSeparator.alpha = 1f
            secondsSeparator.alpha = 1f

            timeSeparator.visibility = VISIBLE

            if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
                secondsSeparator.visibility = VISIBLE
            } else {
                secondsSeparator.visibility = INVISIBLE
            }
        }
    }

    private fun scheduleAutoDeactivate() {
        val delay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
        if (active && delay > 0) {
            Log.d(_tag, "Schedule auto deactivate")
            handler.postDelayed(autoDeactivateTask, delay)
        }
    }

    private fun cancelAutoDeactivate() {
        Log.d(_tag, "Cancel auto deactivate")
        handler.removeCallbacks(autoDeactivateTask)
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            if (isOddSecond(time)) {
                animations.blinkTimeSeparator(timeSeparator)
                if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
                    animations.blinkSecondsSeparator(secondsSeparator)
                }
            }
        }
    }

    private fun playTickSound() {
        if ((!active && !ready) || (active && ready) || settings.getBoolean(PREF_TICK_SOUND_ALWAYS)) {
            tickPlayer.play()
        }
    }

    private fun minTextBrightness() = settings.getInt(PREF_MIN_BRIGHTNESS) / 100f

    private fun isOddSecond(time: Date) = time.time / 1000 % 2 != 0L

}