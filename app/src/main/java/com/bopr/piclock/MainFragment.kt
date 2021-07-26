package com.bopr.piclock

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.GONE
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.fragment.app.Fragment
import com.bopr.piclock.BrightnessControl.Companion.MAX_BRIGHTNESS
import com.bopr.piclock.BrightnessControl.Companion.MIN_BRIGHTNESS
import com.bopr.piclock.ContentLayoutType.*
import com.bopr.piclock.ContentLayoutType.Companion.layoutTypeOf
import com.bopr.piclock.ScaleControl.Companion.MAX_SCALE
import com.bopr.piclock.ScaleControl.Companion.MIN_SCALE
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_COLORS
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_LAYOUT
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_STYLE
import com.bopr.piclock.util.*
import java.util.*

/**
 * Main application fragment.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SuppressLint("ClickableViewAccessibility")
class MainFragment : Fragment(), OnSharedPreferenceChangeListener, Contextual {

    private val timer = TimeControl(::onTimer)
    private val settings by lazy { Settings(this) }

    private val rootView by lazy {
        requireView() as ConstraintLayout
    }

    private val contentHolderView by lazy {
        rootView.findViewById<ViewGroup>(R.id.content_holder).apply {
            setOnTouchListener { _, _ -> false } /* translate all touches to parent */
        }
    }

    private val controlSet by lazy {
        setOf(
            fullscreenControl,
            soundControl,
            floatControl,
            autoInactivateControl,
            brightnessControl,
            scaleControl,
            layoutControl,
            contentControl
        )
    }

    private val fullscreenControl by lazy {
        FullscreenControl(requireActivity(), settings)
    }

    private val soundControl by lazy {
        SoundControl(requireContext(), settings)
    }

    private val floatControl by lazy {
        FloatControl(contentHolderView, settings).apply {
            onFloat = { soundControl.onViewFloating(it) }
        }
    }

    private val autoInactivateControl by lazy {
        AutoInactivateControl(settings).apply {
            onInactivate = { setMode(MODE_INACTIVE, true) }
        }
    }

    private val brightnessControl by lazy {
        BrightnessControl(contentHolderView, settings).apply {
            onSwipeStart = {
                floatControl.pause()
                setMode(MODE_INACTIVE, true)
                infoView.fadeInShow()
            }
            onSwipe = { brightness ->
                infoView.text = getString(
                    when (brightness) {
                        MAX_BRIGHTNESS -> R.string.brightness_info_max
                        MIN_BRIGHTNESS -> R.string.brightness_info_min
                        else -> R.string.brightness_info
                    }, brightness
                )
            }
            onSwipeEnd = {
                infoView.fadeOutHide()
                floatControl.resume()
            }
        }
    }

    private val scaleControl by lazy {
        ScaleControl(contentHolderView, settings).apply {
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
            onPinchEnd = {
                infoView.fadeOutHide()
                floatControl.resume()
            }
        }
    }

    private val layoutControl by lazy {
        LayoutControl(rootView, parentFragmentManager, settings)
    }

    private val contentControl by lazy {
        ContentControlWrapper()
    }

    private lateinit var infoView: TextView

    @Mode
    private var mode = MODE_ACTIVE
    private var currentLayoutResId = 0
    private var currentStyleResId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings.addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false).apply {
            setOnApplyWindowInsetsListener(this) { _, insets ->
                fitIntoWindowOnce(insets)  /* fit once to avoid flickering of status areas */
                insets
            }

            setOnTouchListener { _, event ->
                autoInactivateControl.onTouch(event) || brightnessControl.onTouch(event)
                        || scaleControl.onTouch(event)
            }

            setOnClickListener {
                when (mode) {
                    MODE_ACTIVE -> setMode(MODE_INACTIVE, true)
                    MODE_INACTIVE -> setMode(MODE_ACTIVE, true)
                }
            }

            findViewById<View>(R.id.settings_button).apply {
                setOnClickListener {
                    when (mode) {
                        MODE_ACTIVE,
                        MODE_INACTIVE -> setMode(MODE_EDITOR, true)
                        MODE_EDITOR -> setMode(MODE_INACTIVE, true)
                    }
                }
                visibility = GONE
            }

            findViewById<View>(R.id.settings_container).apply {
                visibility = GONE
            }

            infoView = findViewById<TextView>(R.id.info_view).apply {
                visibility = GONE
            }
        }
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        createContentControl()
        savedState?.apply {
            setMode(getInt(STATE_KEY_MODE), false)
        } ?: apply {
            setMode(MODE_INACTIVE, true)
        }
    }

    override fun onSaveInstanceState(savedState: Bundle) {
        super.onSaveInstanceState(savedState)
        savedState.apply {
            putInt(STATE_KEY_MODE, mode)
        }
    }

    override fun onDestroy() {
        settings.removeListener(this)
        controlSet.forEach { if (it is Destroyable) it.destroy() }
        super.onDestroy()
    }

    override fun onPause() {
        timer.pause()
        autoInactivateControl.pause()
        floatControl.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        floatControl.resume()
        autoInactivateControl.resume()
        timer.resume()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        Log.d(TAG, "Setting: $key has changed to: ${settings.all[key]}")

        when (key) {
            PREF_CONTENT_LAYOUT,
            PREF_CONTENT_STYLE,
            PREF_CONTENT_COLORS -> createContentControl()
        }

        controlSet.forEach { it.onSettingChanged(key) }
    }

    internal fun onBackPressed(): Boolean {
        return if (mode == MODE_EDITOR) {
            setMode(MODE_INACTIVE, true)
            true
        } else {
            false
        }
    }

    private fun onTimer(time: Date, tick: Int) {
        controlSet.forEach { it.onTimer(time, tick) }

//        Log.v(TAG, "Time: $time, tick:$tick")
    }

    private fun setMode(@Mode newMode: Int, animate: Boolean) {
        mode = newMode
        controlSet.forEach { it.onModeChanged(mode, animate) }

        Log.d(TAG, "Mode set to: $mode")
    }

    private fun createContentControl() {
        val layoutName = settings.getString(PREF_CONTENT_LAYOUT)
        val layoutResId = requireResId(layoutName)
        val styleResId = requireResId(settings.contentLayoutStyleName)

        if (layoutResId == currentLayoutResId && styleResId == currentStyleResId) return

        currentLayoutResId = layoutResId
        currentStyleResId = styleResId

        val contentView = layoutInflater.inflateWithTheme(
            currentLayoutResId, contentHolderView, false, currentStyleResId
        )

        contentHolderView.apply {
            removeAllViews()
            addView(contentView)
        }

        contentControl.setControl(
            when (layoutTypeOf(layoutResId)) {
                DIGITAL -> DigitalClockControl(contentView, settings)
                ANALOG_TEXT_DATE -> AnalogClockTextDateControl(contentView, settings)
                ANALOG_BARS_DATE -> AnalogClockBarsDateControl(contentView, settings)
            }
        )

        Log.d(TAG, "Created content: ${settings.contentLayoutStyleName}")
    }

    /**
     * For debug purposes only. Allows to force time to go faster or slower :)
     */
    internal fun setTimeParams(multiplier: Float, increment: Long) {
        Log.w(TAG, "FAKE TIME: multiplier = $multiplier, increment = $increment")

        timer.apply {
            if (multiplier > 0) fakeTimeMultiplier = multiplier
            if (increment > 0) fakeTimeIncrement = increment
        }
    }

    @IntDef(value = [MODE_ACTIVE, MODE_INACTIVE, MODE_EDITOR])
    annotation class Mode

    companion object {

        private const val TAG = "MainFragment"
        private const val STATE_KEY_MODE = "mode"

        const val MODE_INACTIVE = 0
        const val MODE_ACTIVE = 1
        const val MODE_EDITOR = 2
    }
}