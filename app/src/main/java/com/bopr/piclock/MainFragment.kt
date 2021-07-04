package com.bopr.piclock

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
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
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
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
    private val rootView get() = requireView() as ConstraintLayout
    private var currentTime = Date()

    private lateinit var contentView: ViewGroup
    private lateinit var settingsContainer: FragmentContainerView
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

    private val floatControl: FloatContentControl by lazy {
        FloatContentControl(contentView, handler).apply {
            interval = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)
            onBusy = { busy ->
                tickControl.onFloatContent(busy)
            }
        }
    }

    private lateinit var autoDeactivationControl: AutoDeactivationControl

    private val brightnessControl: BrightnessControl by lazy {
        BrightnessControl(requireContext()).apply {
            onChangeBrightness = { inactiveValue, maxValue ->
                currentBrightness = if (mode == MODE_INACTIVE || mode == MODE_EDITOR)
                    inactiveValue
                else
                    maxValue
            }
            onFadeBrightness = { value, onEnd ->
                animations.fadeBrightness(contentView, currentBrightness, value, onEnd)
            }
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
                settings.update { putInt(PREF_INACTIVE_BRIGHTNESS, inactiveBrightness) }
            }
            inactiveBrightness = settings.getInt(PREF_INACTIVE_BRIGHTNESS)
        }
    }

    private lateinit var scaleControl: ScaleControl
    private lateinit var layoutControl: LayoutControl

    private var currentBrightness: Int
        get() = (contentView.alpha * 100).toInt()
        set(value) {
            contentView.alpha = value / 100f
        }

    private var scale: Float by FloatSettingsPropertyDelegate(PREF_CONTENT_SCALE) { settings }
    private var currentScale: Float
        get() = contentView.scaleX
        set(value) {
            contentView.apply {
                scaleX = value
                scaleY = value
            }
        }

    private var mode = MODE_INACTIVE
    private var scaling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(_tag, "Creating fragment")

        super.onCreate(savedInstanceState)

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)

        tickControl = TickControl(requireContext()).apply {
            setSound(settings.getString(PREF_TICK_SOUND))
            setRules(settings.getStringSet(PREF_TICK_RULES))
        }

        autoDeactivationControl = AutoDeactivationControl(handler).apply {
            delay = settings.getLong(PREF_AUTO_DEACTIVATION_DELAY)
            onDeactivate = {
                setMode(MODE_INACTIVE, true)
            }
        }

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
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        fullscreenControl = FullscreenControl(requireActivity(), handler)

        return inflater.inflate(R.layout.fragment_main, container, false).apply {
            setOnClickListener {
                when (mode) {
                    MODE_ACTIVE -> setMode(MODE_INACTIVE, true)
                    MODE_INACTIVE -> setMode(MODE_ACTIVE, true)
                }
            }

            setOnTouchListener { _, event ->
                autoDeactivationControl.onTouch(event, mode)
                        || brightnessControl.onTouch(event)
                        || scaleControl.onTouch(event)
            }

            doOnLayout {
                savedState?.apply {
                    fitContentIntoScreen()
                    setMode(getInt("mode"), false)
                } ?: apply {
                    setMode(MODE_INACTIVE, true)
                }
            }

            setOnApplyWindowInsetsListener(this) { _, windowInsets ->
                adjustMargins(windowInsets)
                windowInsets
            }

            settingsContainer = findViewById<FragmentContainerView>(R.id.settings_container).apply {
                visibility = GONE
            }

            settingsButton = findViewById<FloatingActionButton>(R.id.settings_button).apply {
                visibility = GONE
                setOnClickListener {
                    when (mode) {
                        MODE_ACTIVE, MODE_INACTIVE -> setMode(MODE_EDITOR, true)
                        MODE_EDITOR -> setMode(MODE_INACTIVE, true)
                    }
                }
            }

            infoView = findViewById<TextView>(R.id.info_view).apply {
                visibility = GONE
            }

            contentView = findViewById<ViewGroup>(R.id.content_view).apply {
                setOnTouchListener { _, _ -> false } /* translate onTouch to parent */
            }
            updateContentView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layoutControl = LayoutControl(rootView)
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putInt("mode", mode)
        }
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        tickControl.stop()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onPause() {
        autoDeactivationControl.onPause()
        timer.enabled = false
        floatControl.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        timer.enabled = true
        floatControl.onResume()
        autoDeactivationControl.onResume()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

        settings.apply {
            when (key) {
                PREF_FULLSCREEN_ENABLED ->
                    updateFullscreenControl()
                PREF_CONTENT_LAYOUT ->
                    updateContentView()
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
                PREF_CONTENT_SCALE ->
                    updateScale()
                PREF_DIGITS_ANIMATION ->
                    updateDigitsAnimation()
                PREF_INACTIVE_BRIGHTNESS ->
                    brightnessControl.inactiveBrightness = getInt(PREF_INACTIVE_BRIGHTNESS)
                PREF_TICK_SOUND ->
                    tickControl.setSound(getString(key))
                PREF_TICK_RULES ->
                    tickControl.setRules(getStringSet(key))
                PREF_CONTENT_FLOAT_INTERVAL ->
                    floatControl.interval = getLong(key)
                PREF_AUTO_DEACTIVATION_DELAY ->
                    autoDeactivationControl.delay = getLong(key)
            }
        }
    }

    private fun setMode(newMode: Int, animate: Boolean) {
        val oldMode = mode
        mode = newMode

        autoDeactivationControl.onModeChanged(mode)
        floatControl.onModeChanged(mode)
        fullscreenControl.onModeChanged(mode)
        tickControl.onModeChanged(mode, animate)
        layoutControl.onModeChanged(mode, animate)
        brightnessControl.onModeChanged(newMode, animate)

        Log.d(_tag, "Mode: $mode")
    }

    private fun updateContentView() {
        Log.d(_tag, "Creating content")

        contentView.apply {
            removeAllViews()
            val resId = getResId("layout", settings.getString(PREF_CONTENT_LAYOUT))
            addView(layoutInflater.inflate(resId, this, false).apply {
                hoursView = findViewById(R.id.hours_view)
                minutesView = findViewById(R.id.minutes_view)
                secondsView = findViewById(R.id.seconds_view)
                amPmMarkerView = findViewById(R.id.am_pm_marker_view)
                dateView = findViewById(R.id.date_view)
                timeSeparator = findViewById(R.id.time_separator)
                secondsSeparator = findViewById(R.id.seconds_separator)
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

        dateFormat =
            if (pattern == SYSTEM_DEFAULT) DEFAULT_DATE_FORMAT else defaultDatetimeFormat(pattern)
        dateView.visibility = if (pattern.isEmpty()) GONE else VISIBLE

        fitContentIntoScreen()
    }

    private fun updateSeparatorViews() {
        //todo: fix: when blink disabled separator hides
        if (settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)) {
            timeSeparator.visibility = VISIBLE
            secondsSeparator.visibility =
                if (settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()) VISIBLE else INVISIBLE

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

    private fun updateScale() {
        currentScale = scale
    }

    private fun onTimer() {
//        currentTime = Date(currentTime.time + 10000)
        currentTime = Date()

//        Log.v(_tag, "On timer: $time")

        updateContentViewData()
        blinkTimeSeparator()
        tickControl.onTimer(mode, floatControl.busy)
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
            val pr = contentView.parentView.getScaledRect()
            val vr = contentView.getScaledRect()
            if (pr.width() >= vr.width() && pr.height() >= vr.height()) {
                onEnd()
            } else {
                Log.d(_tag, "Fitting content scale")

                scaling = true
                //todo: move into if out of screen
                animations.fitScaleIntoParent(contentView) {
                    scaling = false
                    onEnd()
                }
            }
        }
    }

    private fun adjustMargins(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(Type.systemBars())
        val fabMargin = resources.fabMargin
        infoView.updateLayoutParams<MarginLayoutParams> {
            leftMargin = fabMargin + insets.left
            topMargin = fabMargin + insets.top
        }
        settingsButton.updateLayoutParams<MarginLayoutParams> {
            rightMargin = fabMargin + insets.right
            bottomMargin = fabMargin + insets.bottom
        }
        settingsContainer.updateLayoutParams<MarginLayoutParams> {
            rightMargin = insets.right
            bottomMargin = insets.bottom
        }
    }

    companion object {

        const val MODE_INACTIVE = 0
        const val MODE_ACTIVE = 1
        const val MODE_EDITOR = 2
    }

}