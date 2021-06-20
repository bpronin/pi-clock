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
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
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
import com.bopr.piclock.Settings.Companion.PREF_INACTIVE_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.getResId
import com.bopr.piclock.util.requireViewByIdCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ClockFragment : Fragment(), OnSharedPreferenceChangeListener {

    //todo: separate date view into 'date' and 'day name'

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

    private lateinit var scaleControl: ScaleControl
    private lateinit var brightnessControl: BrightnessControl

    private val locale = Locale.getDefault()
    private val autoInactivateTask = Runnable { onAutoInactivate() }
    private val timerTask = Runnable { onTimer() }
    private val handler = Handler(Looper.getMainLooper())

    private var active = false
    private var ready = false
    private var autoInactivating = false

    private val minBrightness = 10
    private val maxBrightness = 100
    private var brightness: Int
        get() = settings.getInt(PREF_INACTIVE_BRIGHTNESS)
        set(value) {
            settings.update { putInt(PREF_INACTIVE_BRIGHTNESS, value) }
        }
    private var currentBrightness: Int
        get() = (contentView.alpha * 100).toInt()
        set(value) {
            contentView.alpha = value / 100f
        }

    private val minScale
        get() = resources.getStringArray(R.array.scale_values).first().toFloat()
    private val maxScale
        get() = resources.getStringArray(R.array.scale_values).last().toFloat()
    private var scale: Float
        get() = settings.getFloat(PREF_CLOCK_SCALE)
        set(value) {
            settings.update { putFloat(PREF_CLOCK_SCALE, value) }
        }
    private var currentScale: Float
        get() = contentView.scaleX
        set(value) {
            contentView.apply {
                scaleX = value
                scaleY = value
            }
        }

    private val floatContentTask = Runnable { onFloatContent() }
    private val floatContentInterval
        get() = settings.getLong(PREF_CLOCK_FLOAT_INTERVAL)

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
        val root = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup

        root.setOnClickListener {
            setActive(!active, true)
        }

        root.setOnTouchListener { _, event ->
            when (event?.action) {
                ACTION_DOWN -> cancelAutoInactivate()
                ACTION_UP -> scheduleAutoInactivate()
            }
            (!active && brightnessControl.processTouch(event)) || scaleControl.processTouch(event)
        }

        settingsButton = root.requireViewByIdCompat(R.id.settings_button)
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        contentView = root.requireViewByIdCompat(R.id.content_container)
        contentView.setOnClickListener {
            animations.floatContentHome(contentView)
        }

        scaleControl = ScaleControl(requireContext()).apply {
            onPinchStart = {
                currentScale
            }
            onPinch = { factor ->
                currentScale = max(minScale, min(factor, maxScale))
            }
            onPinchEnd = {
                scale = currentScale
            }
        }

        brightnessControl = BrightnessControl(requireContext()).apply {
            onStartSlide = {
                currentBrightness
            }
            onSlide = { delta ->
                currentBrightness = max(minBrightness, min(delta, maxBrightness))
            }
            onEndSlide = {
                brightness = currentBrightness
            }
        }

        createContentView()

        setActive(savedState?.getBoolean("active") ?: false, false)

        settings.registerOnSharedPreferenceChangeListener(this)

        return root
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
        onTimer()
        scheduleFloatContent()
    }

    override fun onPause() {
        cancelFloatContent()
        cancelTimerTask()
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

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
                    tickPlayer.soundName = getString(PREF_TICK_SOUND)
                PREF_INACTIVE_BRIGHTNESS ->
                    updateBrightness()
                PREF_CLOCK_SCALE ->
                    updateScale()
                PREF_CLOCK_FLOAT_INTERVAL ->
                    updateFloatContentInterval()
            }
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
        updateScale()
    }

    private fun setActive(value: Boolean, animate: Boolean) {
        Log.d(_tag, "Activating: $value, animated: $animate")

        cancelAutoInactivate()
        active = value
        ready = false
        onReady(active)
        updateControls(animate) {
            Log.d(_tag, "Activate complete: $active")

            ready = true
            scheduleAutoInactivate()

        }
    }

    private fun updateControls(animate: Boolean, onComplete: () -> Unit) {
        Log.d(_tag, "Updating controls. active: $active, animated: $animate")

        if (animate) {
            if (active) {
                animations.showFab(settingsButton)
                animations.fadeInContent(
                    contentView,
                    currentBrightness,
                    maxBrightness
                ) {
                    updateBrightness()
                }
                fadeInTickSoundVolume()
                onComplete()
            } else {
                animations.hideFab(settingsButton)
                animations.fadeOutContent(
                    contentView,
                    currentBrightness,
                    brightness
                ) {
                    updateBrightness()
                    onComplete()
                }
                fadeOutTickSoundVolume()
            }
        } else {
            settingsButton.visibility = if (active) VISIBLE else GONE
            updateBrightness()
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

    private fun updateBrightness() {
        currentBrightness = if (active) maxBrightness else brightness
    }

    private fun updateScale() {
        currentScale = scale
    }

    private fun scheduleTimerTask() {
        if (isResumed) {
            cancelTimerTask()
            handler.postDelayed(timerTask, 1000)

//            Log.d(_tag, "Timer scheduled")
        }
    }

    private fun cancelTimerTask() {
        stopTickSound()
        handler.removeCallbacks(timerTask)

//        Log.d(_tag, "Timer canceled")
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

    private fun scheduleAutoInactivate() {
        if (isResumed && active && !autoInactivating) {
            cancelAutoInactivate()
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

    private fun updateFloatContentInterval() {
        if (floatContentInterval > 0) {
            scheduleFloatContent()
        } else {
            animations.floatContentHome(contentView)
        }
    }

    private fun scheduleFloatContent() {
        if (isResumed) {
            cancelFloatContent()
            if (floatContentInterval > 0) {
                handler.postDelayed(floatContentTask, floatContentInterval)

                Log.d(_tag, "Floating scheduled")
            }
        }
    }

    private fun cancelFloatContent() {
        handler.removeCallbacks(floatContentTask)

        Log.d(_tag, "Floating cancelled")
    }

    private fun onFloatContent() {
        Log.d(_tag, "Start floating")

        animations.floatContentSomewhere(contentView) {
            Log.d(_tag, "End floating")

            scheduleFloatContent()
        }
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            val oddSecond = time.time / 1000 % 2 != 0L
            if (oddSecond) {
                animations.blinkTimeSeparator(timeSeparator)
                if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
                    animations.blinkSecondsSeparator(secondsSeparator)
                }
            }
        }
    }

    private fun stopTickSound() {
        tickPlayer.stop()
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

}