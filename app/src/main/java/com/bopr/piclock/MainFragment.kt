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
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
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
import com.bopr.piclock.util.fabMargin
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

    private val animations = Animations()
    private val handler = Handler(Looper.getMainLooper())
    private val timer = HandlerTimer(handler, 1000, this::onTimer)
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
    private lateinit var timeSeparator: TextView
    private lateinit var secondsSeparator: TextView
    private lateinit var amPmMarkerView: TextView
    private lateinit var infoView: TextView
    private lateinit var settings: Settings
    private lateinit var hoursFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var dateFormat: DateFormat

    private val fullscreenControl: FullscreenControl by lazy {
        FullscreenControl(requireActivity(), handler)
    }

    private val soundControl: SoundControl by lazy {
        SoundControl(requireContext()).apply {
            setSound(settings.getString(PREF_TICK_SOUND))
            setRules(settings.getStringSet(PREF_TICK_RULES))
        }
    }

    private val floatControl: FloatControl by lazy {
        FloatControl(contentView, handler).apply {
            interval = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)
            onBusy = { busy ->
                soundControl.onFloatContent(busy)
            }
        }
    }

    private val autoInactivateControl: AutoInactivateControl by lazy {
        AutoInactivateControl(handler).apply {
            delay = settings.getLong(PREF_AUTO_INACTIVATE_DELAY)
            onInactivate = {
                setMode(MODE_INACTIVE, true)
            }
        }
    }

    private val brightnessControl: BrightnessControl by lazy {
        BrightnessControl().apply {
            onStartSlide = {
                setMode(MODE_INACTIVE, true)
                animations.showInfo(infoView)
            }
            onSlide = { brightness ->
                infoView.text = getString(R.string.brightness_info, brightness)
            }
            onEndSlide = { brightness ->
                animations.hideInfo(infoView)
                settings.update { putInt(PREF_MUTED_BRIGHTNESS, brightness) }
            }
        }
    }

    private val scaleControl: ScaleControl by lazy {
        ScaleControl().apply {
            onPinchStart = {
                animations.showInfo(infoView)
            }
            onPinch = { scale ->
                infoView.text = getString(R.string.scale_info, scale)
            }
            onPinchEnd = {
                animations.hideInfo(infoView)
            }
            onScaleChanged = { scale ->
                settings.update { putInt(PREF_CONTENT_SCALE, scale) }
            }
        }
    }

    private val layoutControl: LayoutControl by lazy {
        LayoutControl(rootView, parentFragmentManager)
    }

    @Mode
    private var mode = MODE_INACTIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(_tag, "Creating fragment")

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
                autoInactivateControl.onTouch(event)
                        || brightnessControl.onTouch(event)
                        || scaleControl.onTouch(event)
            }

            doOnLayout {
                savedState?.apply {
                    setMode(getInt(STATE_KEY_MODE), false)
                } ?: apply {
                    setMode(MODE_INACTIVE, true)
                }
            }

            setOnApplyWindowInsetsListener(this) { _, windowInsets ->
                adjustMargins(windowInsets)
                windowInsets
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

        brightnessControl.setView(contentView)
        brightnessControl.setMutedBrightness(settings.getInt(PREF_MUTED_BRIGHTNESS))

        scaleControl.setView(contentView)
        scaleControl.setScale(settings.getInt(PREF_CONTENT_SCALE))
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
        soundControl.stop()
        scaleControl.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        timer.enabled = false
        autoInactivateControl.onPause()
        floatControl.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        floatControl.onResume()
        autoInactivateControl.onResume()
        timer.enabled = true
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
                PREF_DIGITS_ANIMATION ->
                    updateDigitsAnimation()
                PREF_CONTENT_SCALE ->
                    scaleControl.setScale(getInt(key))
                PREF_MUTED_BRIGHTNESS ->
                    brightnessControl.setMutedBrightness(getInt(key))
                PREF_TICK_SOUND ->
                    soundControl.setSound(getString(key))
                PREF_TICK_RULES ->
                    soundControl.setRules(getStringSet(key))
                PREF_CONTENT_FLOAT_INTERVAL ->
                    floatControl.interval = getLong(key)
                PREF_AUTO_INACTIVATE_DELAY ->
                    autoInactivateControl.delay = getLong(key)
            }
        }
    }

    fun onBackPressed(): Boolean {
        return if (mode == MODE_EDITOR) {
            setMode(MODE_INACTIVE, true)
            true
        } else false
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
                timeSeparator = findViewById(R.id.time_separator)
                secondsSeparator = findViewById(R.id.seconds_separator)
            })
        }

        updateFullscreenControl()
        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorViews()
        updateDateView()
        updateContentViewData()
        updateDigitsAnimation() /* must be after updateContentData */

        Log.d(_tag, "Created content view")
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

    private fun onTimer() {
//        Log.v(_tag, "On timer: $time")
//        currentTime = Date(currentTime.time + 10000) /* debug time */

        currentTime = Date()
        updateContentViewData()
        blinkTimeSeparator()
        soundControl.onTimer(mode, floatControl.busy)
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

    //todo: move to layoutControl ?
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

    @IntDef(value = [MODE_ACTIVE, MODE_INACTIVE, MODE_EDITOR])
    annotation class Mode

    companion object {

        const val MODE_INACTIVE = 0
        const val MODE_ACTIVE = 1
        const val MODE_EDITOR = 2

        const val STATE_KEY_MODE = "mode"
    }

}