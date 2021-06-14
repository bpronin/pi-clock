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
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_FULLSCREEN_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
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

    private var autoDeactivateDelay: Long = 1000L

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
        active = false
    }

    private var active = false
        set(value) {
            if (field != value) {
                field = value

                Log.d(_tag, "Active:` $field")

                handler.removeCallbacks(autoDeactivateTask)
                activate()
            }
        }

    private var activated = false
        set(value) {
            if (field != value) {
                field = value

                Log.d(_tag, "Activate complete: $field")

                if (field && autoDeactivateDelay > 0) {
                    Log.d(_tag, "Schedule auto deactivate")

                    handler.postDelayed(autoDeactivateTask, autoDeactivateDelay)
                }
                onActivate(active)
            }
        }

    private var clockBrightness: Int = 0
        set(value) {
            if (field != value) {
                field = value
            }
        }

    var onActivate: (active: Boolean) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        amPmFormat = SimpleDateFormat("a", locale)
        minutesFormat = SimpleDateFormat("mm", locale)
        secondsFormat = SimpleDateFormat("ss", locale)
        tickPlayer = TickPlayer(requireContext())
        animations = ClockFragmentAnimations()

        settings = Settings(requireContext()).apply {
            autoDeactivateDelay = getLong(PREF_AUTO_FULLSCREEN_DELAY)
            dateFormat = SimpleDateFormat(getString(PREF_DATE_FORMAT), locale)
            tickPlayer.soundName = getString(PREF_TICK_SOUND, null)
            clockBrightness = getInt(PREF_CLOCK_BRIGHTNESS)

            registerOnSharedPreferenceChangeListener(this@ClockFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup
        contentContainer = view.findViewById(R.id.content_container)

        view.setOnClickListener {
            active = !active
        }

        settingsButton = view.requireViewByIdCompat(R.id.settings_button)
        settingsButton.apply {
            visibility = if (active) VISIBLE else INVISIBLE
            setOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
        }

        createContentView()

        return view
    }

//    override fun onStart() {
//        super.onStart()
//        active = true
//    }
//
//    override fun onStop() {
//        active = false
//        super.onStop()
//    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
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
                PREF_TICK_SOUND ->
                    tickPlayer.soundName = getString(PREF_TICK_SOUND, null)
                PREF_AUTO_FULLSCREEN_DELAY ->
                    autoDeactivateDelay = getLong(PREF_AUTO_FULLSCREEN_DELAY)
                PREF_CLOCK_BRIGHTNESS ->
                    clockBrightness = getInt(PREF_CLOCK_BRIGHTNESS)
            }
        }
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

    private fun activate() {
        Log.d(_tag, "Activating: $active")

        val wantControlVolume = !settings.getBoolean(PREF_TICK_SOUND_ALWAYS)
        if (active) {
            animations.showFab(settingsButton)
            animations.fadeInContent(contentContainer, clockBrightness,
                onStart = { animator ->
                    if (wantControlVolume) {
                        tickPlayer.fadeVolume(0f, 1f, animator.duration)
                    }
                    activated = true
                })
        } else {
            animations.hideFab(settingsButton)
            animations.fadeOutContent(contentContainer, clockBrightness,
                onStart = { animator ->
                    if (wantControlVolume) {
                        tickPlayer.fadeVolume(1f, 0f, animator.duration)
                    }
                },
                onEnd = {
                    activated = false
                })
        }

//        settings.update {
//            putString(PREF_LAST_MODE, if (active) MODE_ACTIVE else MODE_INACTIVE)
//        }
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

//        active = settings.getString(PREF_LAST_MODE, MODE_INACTIVE) == MODE_ACTIVE
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
            dateView.visibility = VISIBLE
            dateFormat = SimpleDateFormat(pattern, locale)
        } else {
            dateView.visibility = GONE
        }
    }

    private fun updateTimeSeparatorView() {
        if (!settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            timeSeparator.visibility = VISIBLE
            secondsSeparator.visibility =
                if (settings.getBoolean(PREF_SECONDS_VISIBLE)) VISIBLE else INVISIBLE
        }
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
        if (settings.getBoolean(PREF_TICK_SOUND_ALWAYS) || activated) {
            tickPlayer.play()
        }
    }

    private fun isOddSecond(time: Date) = time.time / 1000 % 2 != 0L

    companion object {

        const val MODE_ACTIVE = "active"
        const val MODE_INACTIVE = "inactive"

    }

}