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
import android.view.ViewGroup.GONE
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.DigitalClockControl.Companion.isDigitalClockLayout
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.Settings.Companion.PREF_MUTED_BRIGHTNESS
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.util.HandlerTimer
import com.bopr.piclock.util.getResAnimator
import com.bopr.piclock.util.getResId
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private val rootView get() = requireView() as ConstraintLayout

    private lateinit var contentHolder: ViewGroup
    private lateinit var settingsContainer: View
    private lateinit var settingsButton: FloatingActionButton
    private lateinit var infoView: TextView
    private lateinit var contentControl: ContentControl

    private val settings by lazy {
        Settings(requireContext())
    }

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
        FloatControl(contentHolder, handler).apply {
            setInterval(settings.getLong(PREF_CONTENT_FLOAT_INTERVAL))
            setAnimator(getResAnimator(settings.getString(PREF_FLOAT_ANIMATION)))
            setAnimated(settings.getBoolean(PREF_ANIMATION_ON))
            onFloat = { soundControl.onFloatView(it) }
        }
    }

    private val autoInactivateControl by lazy {
        AutoInactivateControl(handler, settings).apply {
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

    @Mode
    private var mode = MODE_ACTIVE

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

            contentHolder = findViewById<ViewGroup>(R.id.content_holder).apply {
                setOnTouchListener { _, _ -> false } /* translate onTouch to parent */
            }

            createContentControl()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settings.registerOnSharedPreferenceChangeListener(this)

        brightnessControl.apply {
            setView(contentHolder)
            setMutedBrightness(settings.getInt(PREF_MUTED_BRIGHTNESS))
        }

        scaleControl.apply {
            setView(contentHolder)
            setScale(settings.getInt(PREF_CONTENT_SCALE), false)
        }

        fullscreenControl.setEnabled(settings.getBoolean(PREF_FULLSCREEN_ENABLED))

        contentControl.setAnimated(settings.getBoolean(PREF_ANIMATION_ON))
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")

        settings.apply {
            when (key) {
                PREF_CONTENT_LAYOUT ->
                    createContentControl()
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
                PREF_CONTENT_FLOAT_INTERVAL ->
                    floatControl.setInterval(getLong(key))
                PREF_FLOAT_ANIMATION ->
                    floatControl.setAnimator(getResAnimator(getString(key)))
            }
        }

        autoInactivateControl.onSettingChanged(key)
        contentControl.onSettingChanged(key)
    }

    fun onBackPressed(): Boolean {
        return if (mode == MODE_EDITOR) {
            setMode(MODE_INACTIVE, true)
            true
        } else false
    }

    private fun onTimer(tick: Int) {
        val time = getCurrentTime()
        contentControl.onTimer(time, tick)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun createContentControl() {
        val layout = settings.getString(PREF_CONTENT_LAYOUT)
        val view = layoutInflater.inflate(getResId("layout", layout), contentHolder, false)
        contentHolder.apply {
            removeAllViews()
            addView(view)
        }

        if (isDigitalClockLayout(layout)) {
            contentControl = DigitalClockControl(view, settings)
        } else {
            throw IllegalArgumentException("Unregistered content layout resource: $layout")
        }

        Log.d(_tag, "Created content")
    }

    private fun enableAnimation(enable: Boolean) {
        contentControl.setAnimated(enable)
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

        private const val TIMER_INTERVAL = 500L /* default */
//        private const val TIMER_INTERVAL = 250L
//        private const val TIMER_INTERVAL = 100L

//        private var debugTime = Date().time

        private fun getCurrentTime(): Date {
//            debugTime += 24 * 60 * 60 * 1000L
//            debugTime += 60 * 60 * 1000L
//            debugTime += 60 * TIMER_INTERVAL
//            debugTime += 1000L
//            return Date(debugTime)

            return Date() /* default */
        }

    }

}