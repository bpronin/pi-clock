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
import android.view.WindowInsets
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bopr.piclock.Animations.Companion.FLOAT_CONTENT_DURATION
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_DEACTIVATION_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_INACTIVE_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_MODE
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.Settings.Companion.TICK_ACTIVE
import com.bopr.piclock.Settings.Companion.TICK_FLOATING
import com.bopr.piclock.Settings.Companion.TICK_INACTIVE
import com.bopr.piclock.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.util.*

/**
 * Main application fragment.
 */
class MainFragment : Fragment(), OnSharedPreferenceChangeListener {

    /** Logger tag. */
    private val _tag = "ClockFragment"

    private val animations = Animations()
    private val handler = Handler(Looper.getMainLooper())
    private val timer = HandlerTimer(handler, 1000, this::onTimer)
    private val amPmFormat = defaultDatetimeFormat("a")

    private lateinit var contentView: ViewGroup
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var hoursView: ViewGroup
    private lateinit var minutesView: ViewGroup
    private lateinit var secondsView: ViewGroup
    private lateinit var dateView: ViewGroup
    private lateinit var timeSeparator: TextView
    private lateinit var secondsSeparator: TextView
    private lateinit var amPmMarkerView: TextView
    private lateinit var infoView: TextView
    private lateinit var settings: Settings
    private lateinit var hoursFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var dateFormat: DateFormat
    private lateinit var fullscreenControl: FullscreenControl

    private var active = true

    private var autoDeactivating = false
    private val autoDeactivateTask = Runnable { onAutoDeactivate() }

    private lateinit var brightnessControl: BrightnessControl
    private var inactiveBrightness: Int by IntSettingsPropertyDelegate(PREF_INACTIVE_BRIGHTNESS) { settings }
    private var currentBrightness: Int
        get() = (contentView.alpha * 100).toInt()
        set(value) {
            contentView.alpha = value / 100f
        }

    private lateinit var scaleControl: ScaleControl
    private var scale: Float by FloatSettingsPropertyDelegate(PREF_CONTENT_SCALE) { settings }
    private var currentScale: Float
        get() = contentView.scaleX
        set(value) {
            contentView.apply {
                scaleX = value
                scaleY = value
            }
        }

    private lateinit var tickPlayer: TickPlayer
    private val tickMode get() = settings.getStringSet(PREF_TICK_SOUND_MODE)

    private val floatContentTask = Runnable { onFloatContent() }
    private var floatContentEnabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Floating enabled")

                    scheduleFloatContent()
                } else {
                    Log.d(_tag, "Floating disabled")

                    handler.removeCallbacks(floatContentTask)
                }
            }
        }
    private var floating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(_tag, "Creating fragment")

        super.onCreate(savedInstanceState)

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)

        tickPlayer = TickPlayer(requireContext()).apply {
            soundName = settings.getString(PREF_TICK_SOUND)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_main, container, false) as ViewGroup
        root.apply {
            doOnLayoutComplete {
                doOnInitialLayoutComplete(savedState)
            }
            setOnClickListener {
                setActive(!active, true)
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    ACTION_DOWN -> stopAutoDeactivate()
                    ACTION_UP -> startAutoDeactivate()
                }
                //todo: allow change in any mode
                brightnessControl.processTouch(event) || scaleControl.processTouch(event)
            }
            setOnApplyWindowInsetsListener { _, insets ->
                fixControlsPosition(insets)
                onApplyWindowInsets(insets)
            }
        }

        contentView = root.requireViewByIdCompat(R.id.content_container)
        contentView.setOnTouchListener { _, _ -> false } /* translate onTouch to parent */

        settingsButton = root.requireViewByIdCompat(R.id.settings_button)
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        infoView = root.requireViewByIdCompat(R.id.info_view)
        infoView.visibility = GONE

        fullscreenControl = FullscreenControl(requireActivity().window)

        scaleControl = ScaleControl(requireContext()).apply {
            onPinchStart = {
                animations.showInfo(infoView)
                currentScale
            }
            onPinch = { factor ->
                currentScale = factor
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
            onSlide = { value ->
                currentBrightness = value
                infoView.text = getString(R.string.min_brightness_info, currentBrightness)
            }
            onEndSlide = {
                animations.hideInfo(infoView)
                inactiveBrightness = currentBrightness
            }
        }

        createContentView()

        return root
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putBoolean("active", active)
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
        timer.enabled = true
        floatContentEnabled = true
        startAutoDeactivate()
    }

    override fun onPause() {
        stopAutoDeactivate()
        timer.enabled = false
        floatContentEnabled = false
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

        settings.apply {
            when (key) {
                PREF_FULLSCREEN_ENABLED ->
                    updateFullscreenControl()
                PREF_CONTENT_LAYOUT ->
                    createContentView()
                PREF_TIME_FORMAT ->
                    updateHoursMinutesViews()
                PREF_SECONDS_FORMAT -> {
                    updateSecondsView()
                    updateSeparatorViews()
                }
                PREF_TIME_SEPARATORS_VISIBLE ->
                    updateSeparatorViews()
                PREF_TIME_SEPARATORS_BLINKING ->
                    updateSeparatorViews()
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

    /**
     * Occurs when all views are properly resized according to layout.
     */
    private fun doOnInitialLayoutComplete(savedState: Bundle?) {
        savedState?.apply {
            fitContentIntoScreen()
            setActive(active = getBoolean("active"), animate = false)
        } ?: apply {
            setActive(active = false, animate = true)
        }
    }

    private fun beforeActivate() {
        stopAutoDeactivate()
        floatContentEnabled = false
        if (!active) {
            floatContentHome()
        }
/*      todo: floating
        if (!active) {
            floatContentEnabled = false
            floatContentHome()
        } else {
            stopAutoDeactivate()
        }
*/
    }

    private fun afterActivate() {
        startAutoDeactivate()
        floatContentEnabled = true
/*      todo: floating
        if (!active) {
            floatContentEnabled = true
        } else {
            startAutoDeactivate()
        }
*/
    }

    private fun setActive(active: Boolean, animate: Boolean) {
        beforeActivate()
        this.active = active
        updateRootView(animate)
        afterActivate()

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

        updateFullscreenControl()
        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorViews()
        updateDateView()
        updateScale()
        updateContentData(Date())
    }

    private fun updateContentData(time: Date) {
        animations.apply {
            changeText(hoursView, hoursFormat.format(time))
            changeText(minutesView, minutesFormat.format(time))
            if (secondsView.visibility == VISIBLE) {
                changeText(secondsView, secondsFormat.format(time))
            }
            if (dateView.visibility == VISIBLE) {
                changeText(dateView, dateFormat.format(time))
            }
            if (amPmMarkerView.visibility == VISIBLE) {
                amPmMarkerView.text = amPmFormat.format(time)
            }
        }
    }

    private fun updateRootView(animate: Boolean) {
        Log.d(_tag, "Updating controls. active: $active, animated: $animate")

        fullscreenControl.fullscreen = !active

        if (animate) {
            val wantFadeTickVolume = !tickMode.containsAll(setOf(TICK_ACTIVE, TICK_INACTIVE))

            if (active) {
                if (wantFadeTickVolume) {
                    when {
                        tickMode.contains(TICK_ACTIVE) -> tickPlayer.fadeVolume(4000, 0f, 1f)
                        tickMode.contains(TICK_INACTIVE) -> tickPlayer.fadeVolume(4000, 1f, 0f)
                    }
                }
                animations.showFab(settingsButton)
                animations.fadeBrightness(
                    contentView,
                    currentBrightness,
                    BrightnessControl.MAX_BRIGHTNESS
                )
            } else {
                if (wantFadeTickVolume) {
                    when {
                        tickMode.contains(TICK_ACTIVE) -> tickPlayer.fadeVolume(4000, 1f, 0f)
                        tickMode.contains(TICK_INACTIVE) -> tickPlayer.fadeVolume(4000, 0f, 1f)
                    }
                }
                animations.hideFab(settingsButton)
                animations.fadeBrightness(
                    contentView,
                    currentBrightness,
                    inactiveBrightness
                )
            }
        } else {
            settingsButton.visibility = if (active) VISIBLE else GONE
        }

        updateBrightness()
    }

    private fun updateFullscreenControl() {
        fullscreenControl.enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
    }

    private fun updateHoursMinutesViews() {
        val patterns = settings.getString(PREF_TIME_FORMAT).split(":")
        val hoursPattern = patterns[0]
        val minutesPattern = patterns[1]

        hoursFormat = defaultDatetimeFormat(hoursPattern)
        minutesFormat = defaultDatetimeFormat(minutesPattern)
        amPmMarkerView.visibility = if (hoursPattern.startsWith("h")) VISIBLE else GONE

        fitContentIntoScreen()
    }

    private fun updateSecondsView() {
        val pattern = settings.getString(PREF_SECONDS_FORMAT)
        if (pattern.isNotEmpty()) {
            secondsView.visibility = VISIBLE
            secondsFormat = defaultDatetimeFormat(pattern)
        } else {
            secondsView.visibility = GONE
        }

        fitContentIntoScreen()
    }

    private fun updateDateView() {
        val pattern = settings.getString(PREF_DATE_FORMAT)

        dateFormat = if (pattern == SYSTEM_DEFAULT) {
            DEFAULT_DATE_FORMAT
        } else {
            defaultDatetimeFormat(pattern)
        }

        if (pattern.isEmpty()) {
            dateView.visibility = GONE
        } else {
            dateView.visibility = VISIBLE
        }

        fitContentIntoScreen()
    }

    private fun updateSeparatorViews() {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)) {
            timeSeparator.visibility = VISIBLE
            secondsSeparator.visibility =
                (if (settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()) VISIBLE else INVISIBLE)

            if (!settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)) {
                timeSeparator.alpha = 1f
                secondsSeparator.alpha = 1f
            }
        } else {
            timeSeparator.visibility = INVISIBLE
            secondsSeparator.visibility = INVISIBLE
        }
    }

    private fun updateBrightness() {
        currentBrightness = if (active)
            BrightnessControl.MAX_BRIGHTNESS
        else
            inactiveBrightness
    }

    private fun updateScale() {
        currentScale = scale
    }

    private fun onTimer() {
        val time = Date()

//        Log.v(_tag, "On timer: $time")

        updateContentData(time)
        blinkTimeSeparator(time)
        playTickSound()
    }

    private fun startAutoDeactivate() {
        if (active && !autoDeactivating) {
            stopAutoDeactivate()
            val delay = settings.getLong(PREF_AUTO_DEACTIVATION_DELAY)
            if (delay > 0) {
                autoDeactivating = true
                handler.postDelayed(autoDeactivateTask, delay)

                Log.d(_tag, "Auto-deactivate task scheduled at: $delay ms")
            }
        }
    }

    private fun stopAutoDeactivate() {
        if (autoDeactivating) {
            autoDeactivating = false
            handler.removeCallbacks(autoDeactivateTask)

            Log.d(_tag, "Auto-deactivation canceled")
        }
    }

    private fun onAutoDeactivate() {
        if (active && autoDeactivating) {
            autoDeactivating = false
            setActive(active = false, animate = true)

            Log.d(_tag, "Auto-deactivation complete")
        }
    }

    private fun updateFloatContentInterval() {
        floatContentEnabled = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL) >= 0
        if (!floatContentEnabled) {
            floatContentHome()
        }
    }

    private fun floatContentHome() {
        Log.d(_tag, "Floating home")

        animations.floatContentHome(contentView)
    }

    private fun scheduleFloatContent() {
        Log.d(_tag, "Scheduling floating")

        val interval = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)
        if (!active && floatContentEnabled && interval >= 0) {
            if (interval == 0L) {
                handler.post(floatContentTask)

                Log.d(_tag, "Floating task posted")
            } else if (interval > 0) {
                handler.postDelayed(floatContentTask, interval)

                Log.d(_tag, "Floating task scheduled after: $interval ms")
            }
        }
    }

    private fun onFloatContent() {
        Log.d(_tag, "Start floating animation")

        floating = true

        if (tickMode.contains(TICK_FLOATING)) {
            tickPlayer.fadeVolume(FLOAT_CONTENT_DURATION, 0f, 1f, 0f)
        }

        animations.floatContentSomewhere(contentView) {
            Log.d(_tag, "End floating animation")

            floating = false
            scheduleFloatContent()
        }
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)) {
            if (time.time / 1000 % 2 != 0L) {
                animations.blinkTimeSeparator(timeSeparator)
                if (settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()) {
                    animations.blinkSecondsSeparator(secondsSeparator)
                }
            }
        }
    }

    private fun updateTickSound() {
        tickPlayer.soundName = settings.getString(PREF_TICK_SOUND)
    }

    private fun playTickSound() {
        if ((tickMode.contains(TICK_ACTIVE) && active)
            || (tickMode.contains(TICK_INACTIVE) && !active)
            || (tickMode.contains(TICK_FLOATING) && floating)
            || tickPlayer.changingVolume
        ) {
            tickPlayer.play()
        }
    }

    private fun fitContentIntoScreen(onEnd: () -> Unit = {}) {
        val pr = contentView.getParentView().getScaledRect()
        val vr = contentView.getScaledRect()
        if (pr.width() >= vr.width() && pr.height() >= vr.height()) {
            onEnd()
        } else {
            Log.d(_tag, "Fitting content scale")

            animations.fitScaleIntoParent(contentView) { onEnd() }
        }
    }

    private fun fixControlsPosition(insets: WindowInsets) {
        val systemInsets = getSystemInsetsCompat(insets)

        settingsButton.apply {
            if (x < systemInsets.left) x += systemInsets.left
            if (y < systemInsets.top) y += systemInsets.top
        }
        infoView.apply {
            if (x < systemInsets.left) x += systemInsets.left
            if (y > systemInsets.bottom - height) y -= systemInsets.bottom - height
        }
    }

}