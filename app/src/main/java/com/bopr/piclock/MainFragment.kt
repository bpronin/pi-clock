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
import com.bopr.piclock.Settings.Companion.PREF_AUTO_DEACTIVATION_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_INACTIVE_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_ALWAYS
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min


class MainFragment : Fragment(), OnSharedPreferenceChangeListener {

    //todo: separate date view into 'date' and 'day name'
    //todo: FAB in right horizontal screen position hide by navigation bar
    //todo: option to tick during float animation

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
    private lateinit var infoView: TextView

    private lateinit var settings: Settings
    private lateinit var amPmFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var hoursFormat: DateFormat
    private lateinit var dateFormat: DateFormat
    private lateinit var animations: Animations

    private lateinit var fullscreenControl: FullscreenSupport
    private lateinit var scaleControl: ScaleControl
    private lateinit var brightnessControl: BrightnessControl

    private val handler = Handler(Looper.getMainLooper())

    private var isActive = true

    private val timerTask = Runnable { onTimer() }

    private var isAutoDeactivating = false
    private val autoDeactivateTask = Runnable { onAutoDeactivate() }

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
    private val maxScale = 100f
    private var scale: Float
        get() = settings.getFloat(PREF_CONTENT_SCALE)
        set(value) {
            settings.update { putFloat(PREF_CONTENT_SCALE, value) }
        }
    private var currentScale: Float
        get() = contentView.scaleX
        set(value) {
            contentView.apply {
                scaleX = value
                scaleY = value
            }
        }

    private lateinit var tickPlayer: TickPlayer
    private val isTickAlways
        get() = settings.getBoolean(PREF_TICK_SOUND_ALWAYS)
    private var isTickVolumeFading = false

    private var isContentFloating = false
    private val floatContentTask = Runnable { onFloatContent() }
    private val floatContentInterval
        get() = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(_tag, "Creating fragment")

        super.onCreate(savedInstanceState)

        amPmFormat = defaultDatetimeFormat("a")
        minutesFormat = defaultDatetimeFormat("mm")
        secondsFormat = defaultDatetimeFormat("ss")
        tickPlayer = TickPlayer(requireContext())
        animations = Animations()

        settings = Settings(requireContext()).apply {
            tickPlayer.soundName = getString(PREF_TICK_SOUND, null)
        }
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup
        root.setOnClickListener {
            setActive(!isActive, true)
        }
        root.setOnTouchListener { _, event ->
            when (event.action) {
                ACTION_DOWN -> stopAutoDeactivate()
                ACTION_UP -> startAutoDeactivate()
            }
            //todo: allow change in any mode
            (!isActive && (brightnessControl.processTouch(event)) || scaleControl.processTouch(event))
        }

        contentView = root.requireViewByIdCompat(R.id.content_container)
        contentView.setOnClickListener {
            animations.floatContentSomewhere(contentView)
        }
//        contentView.setOnTouchListener { _, _ -> false } /* translate onTouch to parent */

        settingsButton = root.requireViewByIdCompat(R.id.settings_button)
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        infoView = root.requireViewByIdCompat(R.id.info_view)
        infoView.visibility = GONE

        fullscreenControl = FullscreenSupport(requireActivity().window).apply {
            enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }

        scaleControl = ScaleControl(requireContext()).apply {
            onPinchStart = {
                animations.showInfo(infoView)
                currentScale
            }
            onPinch = { factor ->
                currentScale = max(minScale, min(factor, maxScale))
                infoView.text = getString(R.string.scale_info, currentScale * 100f)
            }
            onPinchEnd = {
                animations.hideInfo(infoView)
                fitContentIntoScreen {
                    scale = currentScale
                }
            }
        }

        brightnessControl = BrightnessControl(requireContext()).apply {
            onStartSlide = {
                animations.showInfo(infoView)
                currentBrightness
            }
            onSlide = { delta ->
                currentBrightness = max(minBrightness, min(delta, maxBrightness))
                infoView.text = getString(R.string.min_brightness_info, currentBrightness)
            }
            onEndSlide = {
                animations.hideInfo(infoView)
                brightness = currentBrightness
            }
        }

        createContentView()

        return root
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        savedState?.apply {
//            isActive = getBoolean("active")
            setActive(active = getBoolean("active"), animate = false)
        } ?: apply {
            view.doOnLayoutComplete {
                setActive(active = false, animate = true)
            }
        }
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putBoolean("active", isActive)
        }
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        tickPlayer.stop()
        fullscreenControl.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        startTimer()
        startFloatContent()
        startAutoDeactivate()
    }

    override fun onPause() {
        stopAutoDeactivate()
        stopTimer()
        stopFloatContent()
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

        settings.apply {
            when (key) {
                PREF_FULLSCREEN_ENABLED ->
                    fullscreenControl.enabled = getBoolean(PREF_FULLSCREEN_ENABLED)
                PREF_CONTENT_LAYOUT ->
                    createContentView()
                PREF_24_HOURS_FORMAT ->
                    updateHoursView()
                PREF_SECONDS_VISIBLE ->
                    updateSecondsView()
                PREF_TIME_SEPARATOR_BLINKING ->
                    updateTimeSeparatorView()
                PREF_DATE_FORMAT ->
                    updateDateView()
                PREF_AUTO_DEACTIVATION_DELAY ->
                    startAutoDeactivate()
                PREF_TICK_SOUND ->
                    updateTickSound()
                PREF_INACTIVE_BRIGHTNESS ->
                    updateBrightness()
                PREF_CONTENT_SCALE ->
                    updateScale()
                PREF_CONTENT_FLOAT_INTERVAL ->
                    updateFloatContentInterval()
            }
        }
    }

    private fun onBeforeActivate() {
        stopAutoDeactivate()
        stopFloatContent()
        floatContentHome()
    }

    private fun onActivate() {
        startAutoDeactivate()
        startFloatContent()
    }

    private fun setActive(active: Boolean, animate: Boolean) {
        onBeforeActivate()
        isActive = active
        updateViewMode(animate)
        onActivate()

        Log.d(_tag, "Active mode: $active")
    }

    private fun createContentView() {
        Log.d(_tag, "Creating content")

        contentView.apply {
            removeAllViews()
            val resId = requireContext().getResId("layout", settings.getString(PREF_CONTENT_LAYOUT))
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
        updateContentView(Date())
    }

    private fun updateContentView(time: Date) {
        hoursView.text = hoursFormat.format(time)
        minutesView.text = minutesFormat.format(time)
        secondsView.text = secondsFormat.format(time)
        amPmMarkerView.text = amPmFormat.format(time)
        dateView.text = dateFormat.format(time)
    }

    private fun updateViewMode(animate: Boolean) {
        Log.d(_tag, "Updating controls. active: $isActive, animated: $animate")

        fullscreenControl.fullscreen = !isActive

        if (animate) {
            if (isActive) {
                fadeInTickSoundVolume()
                animations.showFab(settingsButton)
                animations.fadeInContent(
                    contentView,
                    currentBrightness,
                    maxBrightness
                )
            } else {
                fadeOutTickSoundVolume()
                animations.hideFab(settingsButton)
                animations.fadeOutContent(
                    contentView,
                    currentBrightness,
                    brightness
                )
            }
        } else {
            settingsButton.visibility = if (isActive) VISIBLE else GONE
        }

        updateBrightness()
    }

    private fun updateHoursView() {
        if (settings.getBoolean(PREF_24_HOURS_FORMAT)) {
            hoursFormat = defaultDatetimeFormat("HH")
            amPmMarkerView.visibility = GONE
        } else {
            hoursFormat = defaultDatetimeFormat("h")
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
                defaultDatetimeFormat(pattern)
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
        currentBrightness = if (isActive) maxBrightness else brightness
    }

    private fun updateScale() {
        currentScale = scale
    }

    private fun fitContentIntoScreen(onEnd: () -> Unit = {}) {
        val pr = contentView.getParentView().getScaledRect()
        val vr = contentView.getScaledRect()
        if (pr.width() > vr.width() && pr.height() > vr.height()) {
            onEnd()
        } else {
            Log.d(_tag, "Fitting content scale")

            animations.fitScaleIntoParent(contentView) { onEnd() }
        }
    }

    private fun startTimer() {
        scheduleTimerTask()

        Log.d(_tag, "Timer started")
    }

    private fun stopTimer() {
        handler.removeCallbacks(timerTask)

        Log.d(_tag, "Timer stopped")
    }

    private fun scheduleTimerTask() {
        handler.postDelayed(timerTask, 1000)

//        Log.v(_tag, "Timer task scheduled")
    }

    private fun onTimer() {
        val time = Date()

//        Log.v(_tag, "On timer: $time")

        updateContentView(time)
        blinkTimeSeparator(time)
        playTickSound()
        scheduleTimerTask()
    }

    private fun startAutoDeactivate() {
        if (isActive && !isAutoDeactivating) {
            stopAutoDeactivate()
            val delay = settings.getLong(PREF_AUTO_DEACTIVATION_DELAY)
            if (delay > 0) {
                isAutoDeactivating = true
                handler.postDelayed(autoDeactivateTask, delay)

                Log.d(_tag, "Auto-deactivate task scheduled at: $delay ms")
            }
        }
    }

    private fun stopAutoDeactivate() {
        if (isAutoDeactivating) {
            isAutoDeactivating = false
            handler.removeCallbacks(autoDeactivateTask)

            Log.d(_tag, "Auto-deactivation canceled")
        }
    }

    private fun onAutoDeactivate() {
        if (isActive && isAutoDeactivating) {
            isAutoDeactivating = false
            setActive(active = false, animate = true)

            Log.d(_tag, "Auto-deactivation complete")
        }
    }

    private fun updateFloatContentInterval() {
        if (floatContentInterval >= 0) {
            scheduleFloatContent()
        } else {
            stopFloatContent()
            floatContentHome()
        }
    }

    private fun floatContentHome() {
        Log.d(_tag, "Floating home")

        animations.floatContentHome(contentView)
    }

    private fun startFloatContent() {
        if (!isActive && !isContentFloating && floatContentInterval >= 0L) {
            isContentFloating = true
            scheduleFloatContent()

            Log.d(_tag, "Floating started")
        }
    }

    private fun stopFloatContent() {
        if (isContentFloating) {
            isContentFloating = false
            handler.removeCallbacks(floatContentTask)

            Log.d(_tag, "Floating stopped")
        }
    }

    private fun scheduleFloatContent() {
        if (isContentFloating) {
            when {
                floatContentInterval == 0L -> {
                    handler.post(floatContentTask)
                }
                floatContentInterval > 0L -> {
                    handler.postDelayed(floatContentTask, floatContentInterval)
                }
                else -> {
                    Log.w(_tag, "Trying to set illegal floating delay: $floatContentInterval ms")
                }
            }

            Log.d(_tag, "Floating task scheduled after: $floatContentInterval ms")
        }
    }

    private fun onFloatContent() {
        Log.d(_tag, "Start floating animation")

        animations.floatContentSomewhere(contentView) {
            Log.d(_tag, "End floating animation")

            scheduleFloatContent()
        }
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            if (time.time / 1000 % 2 != 0L) { /* odd seconds only */
                animations.blinkTimeSeparator(timeSeparator)
                if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
                    animations.blinkSecondsSeparator(secondsSeparator)
                }
            }
        }
    }

    private fun updateTickSound() {
        tickPlayer.soundName = settings.getString(PREF_TICK_SOUND)
    }

    private fun playTickSound() {
        if (isTickAlways || isActive || isTickVolumeFading) {
            Log.v(_tag, "Playing. active; $isActive, fading: $isTickVolumeFading")

            tickPlayer.play()
        }
    }

    private fun fadeInTickSoundVolume() {
        if (!isTickAlways) {
            isTickVolumeFading = true
            tickPlayer.fadeInVolume {
                isTickVolumeFading = false
            }
        }
    }

    private fun fadeOutTickSoundVolume() {
        if (!isTickAlways) {
            isTickVolumeFading = true
            tickPlayer.fadeOutVolume {
                isTickVolumeFading = false
            }
        }
    }

}