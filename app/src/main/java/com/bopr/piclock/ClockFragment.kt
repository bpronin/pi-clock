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
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.ui.BaseFragment
import com.bopr.piclock.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    /**
     * Logger tag.
     */
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
    private var autoDeactivateDelay: Long = 1000L

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
                handler.removeCallbacks(autoDeactivateTask)
                activate()
            }
        }

    private var activated = false
        set(value) {
            if (field != value) {
                field = value
                if (autoDeactivateDelay > 0) {
                    handler.postDelayed(autoDeactivateTask, autoDeactivateDelay)
                }
                onActivate(active)
            }
        }

    var onActivate: (active: Boolean) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locale = Locale.getDefault()
        amPmFormat = SimpleDateFormat("a", locale)
        minutesFormat = SimpleDateFormat("mm", locale)
        secondsFormat = SimpleDateFormat("ss", locale)

        tickPlayer = TickPlayer(requireContext())

        settings = Settings(requireContext()).also {
            autoDeactivateDelay = it.getLong(PREF_AUTO_FULLSCREEN_DELAY)
            tickPlayer.soundName = it.getString(PREF_TICK_SOUND, null)

            it.registerOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tickPlayer.stop()
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
        applySettings()

        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        tickPlayer.stop()
        handler.removeCallbacks(timerTask)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_CLOCK_LAYOUT ->
                createContentView()
            PREF_TICK_SOUND ->
                tickPlayer.soundName = settings.getString(PREF_TICK_SOUND, null)
            PREF_AUTO_FULLSCREEN_DELAY ->
                autoDeactivateDelay = settings.getLong(PREF_AUTO_FULLSCREEN_DELAY)
        }

        applySettings()
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
        val startDelay = 100L
        val duration: Long = 3000

        if (active) {
            settingsButton.showAnimated(R.anim.fab_show, startDelay)

            contentContainer.animateRes(R.anim.fade_in_50, startDelay,
                duration,
                onStart = {
                    if (wantControlVolume) {
                        tickPlayer.fadeVolume(0f, 1f, duration)
                    }
                    activated = true
                })
        } else {
            settingsButton.hideAnimated(R.anim.fab_hide, startDelay)

            contentContainer.animateRes(R.anim.fade_out_50, startDelay,
                duration,
                onStart = {
                    if (wantControlVolume) {
                        tickPlayer.fadeVolume(1f, 0f, duration)
                    }
                },
                onEnd = {
                    activated = false
                })
        }
    }

    private fun createContentView() {
        contentContainer.removeAllViews()
        val resName = settings.getString(PREF_CLOCK_LAYOUT)
        val resId = requireContext().getResourceId("layout", resName)
        contentContainer.addView(layoutInflater.inflate(resId, contentContainer, false).apply {
            hoursView = requireViewByIdCompat(R.id.hours_view)
            minutesView = requireViewByIdCompat(R.id.minutes_view)
            secondsView = requireViewByIdCompat(R.id.seconds_view)
            amPmMarkerView = requireViewByIdCompat(R.id.am_pm_marker_view)
            dateView = requireViewByIdCompat(R.id.date_view)
            timeSeparator = requireViewByIdCompat(R.id.time_separator)
            secondsSeparator = requireViewByIdCompat(R.id.seconds_separator)
        })
    }

    private fun applySettings() {
        val locale = Locale.getDefault()
        dateFormat = SimpleDateFormat(settings.getString(PREF_DATE_FORMAT), locale)

        timeSeparator.visibility = VISIBLE

        if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
            secondsView.visibility = VISIBLE
            secondsSeparator.visibility = VISIBLE
        } else {
            secondsSeparator.visibility = GONE
            secondsView.visibility = GONE
        }

        if (settings.getBoolean(PREF_24_HOURS_FORMAT)) {
            hoursFormat = SimpleDateFormat("HH", locale)
            amPmMarkerView.visibility = GONE
        } else {
            hoursFormat = SimpleDateFormat("h", locale)
            amPmMarkerView.visibility = VISIBLE
        }

        if (settings.getBoolean(PREF_DATE_VISIBLE)) {
            dateView.visibility = VISIBLE
        } else {
            dateView.visibility = GONE
        }


//      todo:  active = settings.getBoolean(PREF_LAST_ACTIVE)
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            val secondsVisible = settings.getBoolean(PREF_SECONDS_VISIBLE)
            if (isOddSecond(time)) {
                timeSeparator.showAnimated(R.anim.time_separator_show, 0)
                if (secondsVisible) {
                    secondsSeparator.showAnimated(R.anim.time_separator_show, 0)
                }
            } else {
                timeSeparator.hideAnimated(R.anim.time_separator_hide, 0)
                if (secondsVisible) {
                    secondsSeparator.hideAnimated(R.anim.time_separator_hide, 0)
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

}