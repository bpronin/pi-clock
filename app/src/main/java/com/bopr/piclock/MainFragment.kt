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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_DEACTIVATION_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_INACTIVE_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.util.*

/**
 * Main application fragment.
 */
class MainFragment : Fragment(), OnSharedPreferenceChangeListener {

    private val _tag = "MainFragment"

    private val animations = Animations()
    private val handler = Handler(Looper.getMainLooper())
    private val timer = HandlerTimer(handler, 1000, this::onTimer)
    private val amPmFormat = defaultDatetimeFormat("a")
    private var currentTime = Date()

    private lateinit var contentView: ViewGroup
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var hoursView: AnimatedTextView
    private lateinit var minutesView: AnimatedTextView
    private lateinit var secondsView: AnimatedTextView
    private lateinit var dateView: AnimatedTextView
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
    private lateinit var tickControl: TickControl

    private var mode = MODE_INACTIVE

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
    private var scaling: Boolean = false

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

        fullscreenControl = FullscreenControl(requireActivity())
        tickControl = TickControl(requireContext(), settings)

        scaleControl = ScaleControl(requireContext()).apply {
            onPinchStart = {
                scaling = true
                animations.showInfo(infoView)
                currentScale
            }
            onPinch = { factor ->
                currentScale = factor
                infoView.text = getString(R.string.scale_info, currentScale * 100f)
            }
            onPinchEnd = {
                animations.hideInfo(infoView)
                scaling = false
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
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {


        val root = inflater.inflate(R.layout.fragment_main, container, false).apply {
            doOnLayout {
                doOnInitialLayoutComplete(savedState)
            }

            setOnClickListener {
                when (mode) {
                    MODE_ACTIVE -> setMode(MODE_INACTIVE, true)
                    MODE_INACTIVE -> setMode(MODE_ACTIVE, true)
                    MODE_EDITOR -> TODO()
                }
            }

            setOnTouchListener { _, event ->
                when (event.action) {
                    ACTION_DOWN -> stopAutoDeactivate()
                    ACTION_UP -> startAutoDeactivate()
                }
                //todo: allow change in any mode
                brightnessControl.processTouch(event) || scaleControl.processTouch(event)
            }

            settingsButton = viewById<FloatingActionButton>(R.id.settings_button).apply {
                setOnClickListener {
                    showPreferencesView()
                }
                ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                    val insets = windowInsets.getInsets(Type.systemBars())
                    val margin = resources.getDimension(R.dimen.fab_margin).toInt()
                    view.updateLayoutParams<MarginLayoutParams> {
                        rightMargin = margin + insets.right
                        bottomMargin = margin + insets.bottom
                    }
                    windowInsets
                }
            }

            infoView = viewById<TextView>(R.id.info_view).apply {
                visibility = GONE
                ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                    val insets = windowInsets.getInsets(Type.systemBars())
                    val margin = resources.getDimension(R.dimen.fab_margin).toInt()
                    view.updateLayoutParams<MarginLayoutParams> {
                        leftMargin = margin + insets.left
                        topMargin = margin + insets.top
                    }
                    windowInsets
                }
            }

            contentView = viewById<ViewGroup>(R.id.content_container).apply {
                setOnTouchListener { _, _ -> false } /* translate onTouch to parent */
            }
        }

        createContentView()

        return root
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putInt("mode", mode)
        }
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        tickControl.destroy()
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
                PREF_INACTIVE_BRIGHTNESS ->
                    updateBrightness()
                PREF_CONTENT_SCALE ->
                    updateScale()
                PREF_CONTENT_FLOAT_INTERVAL ->
                    updateFloatContentInterval()
                PREF_DIGITS_ANIMATION ->
                    updateDigitsAnimation()
            }
        }
    }

    /**
     * Occurs when all views are properly resized according to layout.
     */
    private fun doOnInitialLayoutComplete(savedState: Bundle?) {
        savedState?.apply {
            fitContentIntoScreen()
            setMode(getInt("mode"), false)
        } ?: apply {
            setMode(MODE_INACTIVE, true)
        }
    }

    private fun beforeSetMode() {
        stopAutoDeactivate()
        floatContentEnabled = false
        if (mode == MODE_INACTIVE) {
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

    private fun afterSetMode() {
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

    private fun setMode(mode: Int, animate: Boolean) {
        beforeSetMode()
        this.mode = mode
        updateRootView(animate)
        afterSetMode()

        Log.d(_tag, "Mode: $mode")
    }

    private fun createContentView() {
        Log.d(_tag, "Creating content")

        contentView.apply {
            removeAllViews()
            val resId = getResId("layout", settings.getString(PREF_CONTENT_LAYOUT))
            addView(layoutInflater.inflate(resId, this, false).apply {
                hoursView = viewById(R.id.hours_view)
                minutesView = viewById(R.id.minutes_view)
                secondsView = viewById(R.id.seconds_view)
                amPmMarkerView = viewById(R.id.am_pm_marker_view)
                dateView = viewById(R.id.date_view)
                timeSeparator = viewById(R.id.time_separator)
                secondsSeparator = viewById(R.id.seconds_separator)
            })
        }

        updateFullscreenControl()
        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorViews()
        updateDateView()
        updateScale()
        updateContentViewData()
        updateDigitsAnimation() /* must be after updateContentData */
    }

    private fun updateContentViewData() {
        hoursView.setTextAnimated(hoursFormat.format(currentTime))
        minutesView.setTextAnimated(minutesFormat.format(currentTime))
        if (secondsView.visibility == VISIBLE) {
            secondsView.setTextAnimated(secondsFormat.format(currentTime))
        }
        if (dateView.visibility == VISIBLE) {
            dateView.setTextAnimated(dateFormat.format(currentTime))
        }
        if (amPmMarkerView.visibility == VISIBLE) {
            amPmMarkerView.text = amPmFormat.format(currentTime)
        }

        fitContentIntoScreen()
    }

    private fun updateRootView(animate: Boolean) {
        Log.d(_tag, "Updating controls. mode: $mode, animated: $animate")

        fullscreenControl.fullscreen = (mode == MODE_INACTIVE)

        if (animate) {
            tickControl.onChangeMode(mode)

            when (mode) {
                MODE_ACTIVE -> {
                    animations.showFab(settingsButton)
                    animations.fadeBrightness(
                        contentView,
                        currentBrightness,
                        BrightnessControl.MAX_BRIGHTNESS
                    )
                }
                MODE_INACTIVE -> {
                    animations.hideFab(settingsButton)
                    animations.fadeBrightness(
                        contentView,
                        currentBrightness,
                        inactiveBrightness
                    )
                }
                MODE_EDITOR -> TODO()
            }
        } else {
            settingsButton.visibility = if (mode == MODE_INACTIVE) GONE else VISIBLE
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

    private fun updateDigitsAnimation() {
        val resId = getResId("animator", settings.getString(PREF_DIGITS_ANIMATION))

        hoursView.setTextAnimatorRes(resId)
        minutesView.setTextAnimatorRes(resId)
        secondsView.setTextAnimatorRes(resId)
        dateView.setTextAnimatorRes(resId)
    }


    private fun updateBrightness() {
        currentBrightness = if (mode == MODE_INACTIVE)
            inactiveBrightness
        else
            BrightnessControl.MAX_BRIGHTNESS
    }

    private fun updateScale() {
        currentScale = scale
    }

    private fun onTimer() {
//        currentTime = Date(currentTime.time + 10000)
        currentTime = Date()

//        Log.v(_tag, "On timer: $time")

        updateContentViewData()
        blinkTimeSeparator()
        tickControl.onTick(mode, floating)
    }

    private fun startAutoDeactivate() {
        if (mode == MODE_ACTIVE && !autoDeactivating) {
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
        if (mode == MODE_ACTIVE && autoDeactivating) {
            autoDeactivating = false
            setMode(MODE_INACTIVE, true)

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
        if (mode == MODE_INACTIVE && floatContentEnabled && interval >= 0) {
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
        tickControl.onFloatContent(floating)
        animations.floatContentSomewhere(contentView) {
            Log.d(_tag, "End floating animation")

            floating = false
            scheduleFloatContent()
        }
    }

    private fun blinkTimeSeparator() {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)) {
            if (currentTime.time / 1000 % 2 != 0L) {
                animations.blinkTimeSeparator(timeSeparator)
                if (settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()) {
                    animations.blinkSecondsSeparator(secondsSeparator)
                }
            }
        }
    }

    private fun fitContentIntoScreen(onEnd: () -> Unit = {}) {
        if (!scaling) {
            val pr = contentView.getParentView().getScaledRect()
            val vr = contentView.getScaledRect()
            if (pr.width() >= vr.width() && pr.height() >= vr.height()) {
                onEnd()
            } else {
                Log.d(_tag, "Fitting content scale")

                scaling = true
                animations.fitScaleIntoParent(contentView) {
                    scaling = false
                    onEnd()
                }
            }
        }
    }

    private fun showPreferencesView() {
//        settingsView.apply {
//            visibility = if (visibility == VISIBLE) GONE else VISIBLE
//        }
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    companion object {

        const val MODE_INACTIVE = 0
        const val MODE_ACTIVE = 1
        const val MODE_EDITOR = 2
    }

}