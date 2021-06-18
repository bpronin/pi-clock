package com.bopr.piclock

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent.*
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MIN_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.getResId
import com.bopr.piclock.util.requireViewByIdCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Math.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : Fragment(), OnSharedPreferenceChangeListener {

    /** Logger tag. */
    private val _tag = "ClockFragment"

    private lateinit var contentView: ViewGroup
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
    private lateinit var brightnessControl: ClockFragmentBrightnessControl

    private val locale = Locale.getDefault()
    private val autoInactivateTask = Runnable { onAutoInactivate() }
    private val timerTask = Runnable { onTimer() }
    private val moveContentTask = Runnable { onMoveContent() }
    private val handler = Handler(Looper.getMainLooper())

    private var active = false
    private var ready = false
    private var autoInactivating = false

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
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup

        view.setOnClickListener {
            setActive(!active, true)
        }

        view.setOnTouchListener { v, event ->
            when (event?.action) {
                ACTION_DOWN -> cancelAutoInactivate()
                ACTION_UP -> scheduleAutoInactivate()
            }
            brightnessControl.onTouch(event) || scaleControl.onTouch(v, event)
        }

        settingsButton = view.requireViewByIdCompat(R.id.settings_button)
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        contentView = view.findViewById(R.id.content_container)

        scaleControl = ClockFragmentScaleControl(requireContext(), contentView).apply {
            factor = settings.getFloat(PREF_CLOCK_SCALE)
            onEnd = { factor ->
                settings.update { putFloat(PREF_CLOCK_SCALE, factor) }
            }
        }

        brightnessControl =
            ClockFragmentBrightnessControl(requireContext(), contentView).apply {
                minBrightness = settings.getInt(PREF_MIN_BRIGHTNESS)
                onEnd = { brightness ->
                    settings.update { putInt(PREF_MIN_BRIGHTNESS, brightness) }
                }
            }

        createContentView()

        setActive(savedState?.getBoolean("active") ?: false, false)

        settings.registerOnSharedPreferenceChangeListener(this@ClockFragment)

        return view
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.putBoolean("active", active)
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        scheduleTimerTask()
        scheduleMoveContent()
    }

    override fun onPause() {
        handler.removeCallbacks(moveContentTask)
        handler.removeCallbacks(timerTask)
        tickPlayer.stop()
        super.onPause()
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
                PREF_AUTO_INACTIVATE_DELAY ->
                    scheduleAutoInactivate()
                PREF_TICK_SOUND ->
                    tickPlayer.soundName = getString(PREF_TICK_SOUND, null)
                PREF_MIN_BRIGHTNESS ->
                    brightnessControl.minBrightness = getInt(PREF_MIN_BRIGHTNESS)
                PREF_CLOCK_SCALE ->
                    scaleControl.factor = getFloat(PREF_CLOCK_SCALE)
                PREF_CLOCK_FLOAT_INTERVAL ->
                    scheduleMoveContent()
            }
        }
    }

    private fun scheduleTimerTask() {
        handler.removeCallbacks(timerTask)
        handler.postDelayed(timerTask, 1000)
    }

    private fun onTimer() {
        val time = Date()

//        Log.d(_tag, "On timer: $time")

        hoursView.text = hoursFormat.format(time)
        minutesView.text = minutesFormat.format(time)
        secondsView.text = secondsFormat.format(time)
        amPmMarkerView.text = amPmFormat.format(time)
        dateView.text = dateFormat.format(time)

        blinkTimeSeparator(time)
        playTickSound()

        scheduleTimerTask()
    }

    private fun scheduleMoveContent() {
        handler.removeCallbacks(moveContentTask)
        handler.postDelayed(moveContentTask, settings.getInt(PREF_CLOCK_FLOAT_INTERVAL) * 1000L)
    }

    private fun onMoveContent() {
        animations.moveSomewhere(contentView) {
            scheduleMoveContent()
        }
    }

    private fun createContentView() {
        Log.d(_tag, "Creating content")

        contentView.apply {
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
    }

    private fun setActive(value: Boolean, animate: Boolean) {
        Log.d(_tag, "Activating: $value, animated: $animate")

        cancelAutoInactivate()
        active = value
        ready = false

        updateControls(animate) {
            Log.d(_tag, "Activate complete: $active")

            ready = true
            scheduleAutoInactivate()
            onReady(active)
        }
    }

    private fun updateControls(animate: Boolean, onComplete: () -> Unit) {
        Log.d(_tag, "Updating controls. active: $active, animated: $animate")

        if (animate) {
            if (active) {
                animations.showFab(settingsButton)
                animations.fadeInContent(
                    contentView,
                    brightnessControl.brightness,
                    brightnessControl.maxBrightness
                )
                fadeInTickSoundVolume()
                onComplete()
            } else {
                animations.hideFab(settingsButton)
                animations.fadeOutContent(
                    contentView,
                    brightnessControl.brightness,
                    brightnessControl.minBrightness
                ) {
                    onComplete()
                }
                fadeOutTickSoundVolume()
            }
        } else {
            if (active) {
                settingsButton.visibility = VISIBLE
                brightnessControl.setMaxBrightness()
            } else {
                settingsButton.visibility = GONE
                brightnessControl.setMinBrightness()
            }
            onComplete()
        }
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

    private fun scheduleAutoInactivate() {
        if (active && !autoInactivating) {
            val delay = settings.getLong(PREF_AUTO_INACTIVATE_DELAY)
            if (delay > 0) {
                autoInactivating = true
                handler.postDelayed(autoInactivateTask, delay)

                Log.d(_tag, "Auto-inactivate scheduled")
            }
        }
    }

    private fun cancelAutoInactivate() {
        if (autoInactivating) {
            autoInactivating = false
            handler.removeCallbacks(autoInactivateTask)

            Log.d(_tag, "Auto-inactivation canceled")
        }
    }

    private fun onAutoInactivate() {
        if (active && autoInactivating) {
            autoInactivating = false
            setActive(value = false, animate = true)

            Log.d(_tag, "Auto-inactivation complete")
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
        if ((!active && !ready) || (active && ready) || settings.getBoolean(PREF_TICK_SOUND_ALWAYS)) {
            tickPlayer.play()
        }
    }

    private fun fadeInTickSoundVolume() {
        if (!settings.getBoolean(PREF_TICK_SOUND_ALWAYS)) {
            tickPlayer.fadeInVolume(3000)
        }
    }

    private fun fadeOutTickSoundVolume() {
        if (!settings.getBoolean(PREF_TICK_SOUND_ALWAYS)) {
            tickPlayer.fadeOutVolume(6000)
        }
    }

    private fun isOddSecond(time: Date) = time.time / 1000 % 2 != 0L

}