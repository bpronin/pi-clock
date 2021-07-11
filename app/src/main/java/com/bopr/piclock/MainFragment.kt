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
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.HandlerTimer
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.getResAnimator
import com.bopr.piclock.util.getResId
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DateFormat
import java.util.*

/**
 * Main application fragment.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainFragment : Fragment(), OnSharedPreferenceChangeListener {

    private val _tag = "MainFragment"

    private val handler = Handler(Looper.getMainLooper())
    private val timer = HandlerTimer(handler, TIMER_INTERVAL, 4, ::onTimer)
    private val amPmFormat = defaultDatetimeFormat("a")
    private val rootView get() = requireView() as ConstraintLayout
    private var currentTime = Date()

    private lateinit var contentView: ViewGroup
    private lateinit var settingsContainer: View
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var hoursView: AnimatedTextView
    private lateinit var minutesView: AnimatedTextView
    private lateinit var secondsView: AnimatedTextView
    private lateinit var dateView: AnimatedTextView
    private lateinit var minutesSeparator: TextView
    private lateinit var secondsSeparator: TextView
    private lateinit var amPmMarkerView: TextView
    private lateinit var infoView: TextView
    private lateinit var settings: Settings
    private lateinit var hoursFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var dateFormat: DateFormat

    private val fullscreenControl by lazy {
        FullscreenControl(requireActivity(), handler)
    }

    private val soundControl by lazy {
        SoundControl(requireContext()).apply {
            setSound(settings.getString(PREF_TICK_SOUND))
            setRules(settings.getStringSet(PREF_TICK_RULES))
        }
    }

    private val floatControl by lazy {
        FloatControl(contentView, handler).apply {
            setInterval(settings.getLong(PREF_CONTENT_FLOAT_INTERVAL))
            setAnimator(getResAnimator(settings.getString(PREF_FLOAT_ANIMATION)))
            setAnimated(settings.getBoolean(PREF_ANIMATION_ON))
            onFloat = { soundControl.onFloatView(it) }
        }
    }

    private val autoInactivateControl by lazy {
        AutoInactivateControl(handler).apply {
            setDelay(settings.getLong(PREF_AUTO_INACTIVATE_DELAY))
            onInactivate = { setMode(MODE_INACTIVE, true) }
        }
    }

    private val brightnessControl by lazy {
        BrightnessControl().apply {
            onSwipeStart = {
                floatControl.pause()
                setMode(MODE_INACTIVE, true)
                infoView.fadeInShow()
            }
            onSwipe = { brightness ->
                val resId = when (brightness) {
                    MAX_BRIGHTNESS -> R.string.brightness_info_max
                    MIN_BRIGHTNESS -> R.string.brightness_info_min
                    else -> R.string.brightness_info
                }
                infoView.text = getString(resId, brightness)
            }
            onSwipeEnd = { brightness ->
                infoView.fadeOutHide()
                floatControl.resume()
                settings.update { putInt(PREF_MUTED_BRIGHTNESS, brightness) }
            }
        }
    }

    private val scaleControl by lazy {
        ScaleControl().apply {
            onPinchStart = {
                floatControl.pause()
                infoView.fadeInShow()
            }
            onPinch = { scale ->
                val resId = when (scale) {
                    MAX_SCALE -> R.string.scale_info_max
                    MIN_SCALE -> R.string.scale_info_min
                    else -> R.string.scale_info
                }
                infoView.text = getString(resId, scale)
            }
            onPinchEnd = { scale ->
                infoView.fadeOutHide()
                floatControl.resume()
                settings.update { putInt(PREF_CONTENT_SCALE, scale) }
            }
        }
    }

    private val layoutControl by lazy {
        LayoutControl(rootView, parentFragmentManager)
    }

    private val blinkAnimator by lazy {
        BlinkControl(minutesSeparator, secondsSeparator).apply {
            setEnabled(
                settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE) &&
                        settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)
            )
            setSecondsEnabled(settings.getString(PREF_SECONDS_FORMAT).isNotEmpty())
            setAnimated(settings.getBoolean(PREF_ANIMATION_ON))
        }
    }

    @Mode
    private var mode = MODE_ACTIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false).apply {
            setOnClickListener {
                when (mode) {
                    MODE_ACTIVE -> setMode(MODE_INACTIVE, true)
                    MODE_INACTIVE -> setMode(MODE_ACTIVE, true)
                }
            }

            setOnTouchListener { _, event ->
                autoInactivateControl.onTouch(event) || (settings.getBoolean(PREF_GESTURES_ENABLED)
                        && (brightnessControl.onTouch(event) || scaleControl.onTouch(event)))
            }

            doOnLayout {
                savedState?.apply {
                    setMode(getInt(STATE_KEY_MODE), false)
                } ?: apply {
                    setMode(MODE_INACTIVE, true)
                }
            }

            settingsContainer = findViewById<View>(R.id.settings_container).apply {
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

            createContentView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settings.registerOnSharedPreferenceChangeListener(this)

        brightnessControl.apply {
            setView(contentView)
            setMutedBrightness(settings.getInt(PREF_MUTED_BRIGHTNESS))
        }

        scaleControl.apply {
            setView(contentView)
            setScale(settings.getInt(PREF_CONTENT_SCALE), false)
        }

        fullscreenControl.setEnabled(settings.getBoolean(PREF_FULLSCREEN_ENABLED))

        updateContentViewData()
        updateDigitsAnimation()
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putInt(STATE_KEY_MODE, mode)
        }
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        handler.removeCallbacksAndMessages(null)
        soundControl.destroy()
        scaleControl.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        timer.enabled = false
        autoInactivateControl.pause()
        floatControl.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        floatControl.resume()
        autoInactivateControl.resume()
        timer.enabled = true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

        settings.apply {
            when (key) {
                PREF_CONTENT_LAYOUT ->
                    createContentView()
                PREF_TIME_FORMAT ->
                    updateHoursMinutesViews()
                PREF_SECONDS_FORMAT -> {
                    updateSecondsView()
                    updateSeparatorsViews()
                }
                PREF_TIME_SEPARATORS_VISIBLE ->
                    updateSeparatorsViews()
                PREF_TIME_SEPARATORS_BLINKING ->
                    updateSeparatorsViews()
                PREF_DATE_FORMAT ->
                    updateDateView()
                PREF_DIGITS_ANIMATION ->
                    updateDigitsAnimation()
                PREF_ANIMATION_ON ->
                    enableAnimation(settings.getBoolean(key))
                PREF_FULLSCREEN_ENABLED ->
                    updateFullscreenControl(getBoolean(key))
                PREF_CONTENT_SCALE ->
                    scaleControl.setScale(getInt(key), true)
                PREF_MUTED_BRIGHTNESS ->
                    brightnessControl.setMutedBrightness(getInt(key))
                PREF_TICK_SOUND ->
                    soundControl.setSound(getString(key))
                PREF_TICK_RULES ->
                    soundControl.setRules(getStringSet(key))
                PREF_AUTO_INACTIVATE_DELAY ->
                    autoInactivateControl.setDelay(getLong(key))
                PREF_CONTENT_FLOAT_INTERVAL ->
                    floatControl.setInterval(getLong(key))
                PREF_FLOAT_ANIMATION ->
                    floatControl.setAnimator(getResAnimator(getString(key)))
            }
        }
    }

    fun onBackPressed(): Boolean {
        return if (mode == MODE_EDITOR) {
            setMode(MODE_INACTIVE, true)
            true
        } else false
    }

    private fun onTimer(tick: Int) {
        // Log.v(_tag, "On timer: $halfTick")

        currentTime = if (TIME_STEP == 0L)
            Date()
        else
            Date(currentTime.time + TIME_STEP)

        if (tick % 2 == 0) updateContentViewData()
        blinkAnimator.onTimer(tick)
        soundControl.onTimer(tick)
    }

    private fun setMode(@Mode newMode: Int, animate: Boolean) {
        if (mode != newMode) {
            mode = newMode

            autoInactivateControl.onModeChanged(mode)
            floatControl.onModeChanged(mode)
            fullscreenControl.onModeChanged(mode)
            soundControl.onModeChanged(mode, animate)
            layoutControl.onModeChanged(mode, animate)
            brightnessControl.onModeChanged(mode, animate)
            scaleControl.onModeChanged(mode)

            Log.d(_tag, "Mode set to: $mode")
        }
    }

    private fun createContentView() {
        contentView.apply {
            removeAllViews()
            val resId = getResId("layout", settings.getString(PREF_CONTENT_LAYOUT))
            addView(layoutInflater.inflate(resId, this, false).apply {
                hoursView = findViewById(R.id.hours_view)
                minutesView = findViewById(R.id.minutes_view)
                secondsView = findViewById(R.id.seconds_view)
                amPmMarkerView = findViewById(R.id.am_pm_marker_view)
                dateView = findViewById(R.id.date_view)
                minutesSeparator = findViewById(R.id.minutes_separator)
                secondsSeparator = findViewById(R.id.seconds_separator)
            })
        }

        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorsViews()
        updateDateView()

        Log.d(_tag, "Created content view")
    }

    private fun updateContentViewData() {
        val animated = settings.getBoolean(PREF_ANIMATION_ON)

        hoursView.setText(hoursFormat.format(currentTime), animated)
        minutesView.setText(minutesFormat.format(currentTime), animated)
        if (dateView.visibility == VISIBLE) {
            dateView.setText(dateFormat.format(currentTime), animated)
        }
        if (amPmMarkerView.visibility == VISIBLE) {
            amPmMarkerView.text = amPmFormat.format(currentTime)
        }
        if (secondsView.visibility == VISIBLE) {
            secondsView.setText(secondsFormat.format(currentTime), animated)
        }
    }

    private fun updateHoursMinutesViews() {
        val patterns = settings.getString(PREF_TIME_FORMAT).split(":")
        val hoursPattern = patterns[0]
        val minutesPattern = patterns[1]

        hoursFormat = defaultDatetimeFormat(hoursPattern)
        minutesFormat = defaultDatetimeFormat(minutesPattern)
        amPmMarkerView.visibility = if (hoursPattern.startsWith("h")) VISIBLE else GONE
    }

    private fun updateSecondsView() {
        val pattern = settings.getString(PREF_SECONDS_FORMAT)
        if (pattern.isNotEmpty()) {
            secondsView.visibility = VISIBLE
            secondsFormat = defaultDatetimeFormat(pattern)
        } else {
            secondsView.visibility = GONE
        }
    }

    private fun updateDateView() {
        val pattern = settings.getString(PREF_DATE_FORMAT)

        dateFormat =
            if (pattern == SYSTEM_DEFAULT) DEFAULT_DATE_FORMAT else defaultDatetimeFormat(pattern)
        dateView.visibility = if (pattern.isEmpty()) GONE else VISIBLE
    }

    private fun updateSeparatorsViews() {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)) {
            val secondsVisible = settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()
            minutesSeparator.visibility = VISIBLE
            secondsSeparator.visibility = if (secondsVisible) VISIBLE else INVISIBLE

            blinkAnimator.setEnabled(settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING))
            blinkAnimator.setSecondsEnabled(secondsVisible)
        } else {
            minutesSeparator.visibility = INVISIBLE
            secondsSeparator.visibility = INVISIBLE

            blinkAnimator.setEnabled(false)
        }
    }

    private fun updateDigitsAnimation() {
        val resId = getResAnimator(settings.getString(PREF_DIGITS_ANIMATION))

        hoursView.setTextAnimator(resId)
        minutesView.setTextAnimator(resId)
        secondsView.setTextAnimator(resId)
        dateView.setTextAnimator(resId)
    }

    private fun enableAnimation(enable: Boolean) {
        blinkAnimator.setAnimated(enable)
        floatControl.setAnimated(enable)
    }

    private fun updateFullscreenControl(enabled: Boolean) {
        fullscreenControl.setEnabled(enabled)
        layoutControl.onFullscreenEnabled(enabled)
    }

    @IntDef(value = [MODE_ACTIVE, MODE_INACTIVE, MODE_EDITOR])
    annotation class Mode

    companion object {

        const val MODE_INACTIVE = 0
        const val MODE_ACTIVE = 1
        const val MODE_EDITOR = 2

        const val STATE_KEY_MODE = "mode"

        const val TIMER_INTERVAL = 500L
        const val TIME_STEP = 0L

//        const val TIMER_INTERVAL = 10L
//        const val TIME_STEP = 1000L
//        const val TIME_STEP = 60 * 1000L
//        const val TIME_STEP = 10 * 60 * 1000L
    }

}