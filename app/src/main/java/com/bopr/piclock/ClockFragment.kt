package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.databinding.FragmentMainBinding
import com.bopr.piclock.util.hideAnimated
import com.bopr.piclock.util.showAnimated
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var binding: FragmentMainBinding
    private lateinit var amPmFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var hoursFormat: DateFormat
    private lateinit var dateFormat: DateFormat
    private val handler = Handler(Looper.getMainLooper())
    private val timerTask = object : Runnable {

        override fun run() {
            onTimer()
            handler.postDelayed(this, 1000)
        }
    }
    private var controlsVisible = false
    private var tickSound: MediaPlayer? = null

    var onClick: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)

        val locale = Locale.getDefault()
        amPmFormat = SimpleDateFormat("a", locale)
        minutesFormat = SimpleDateFormat("mm", locale)
        secondsFormat = SimpleDateFormat("ss", locale)
    }

    override fun onDestroy() {
        super.onDestroy()
        tickSound?.release()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)
        applySettings()

        binding.settingsButton.apply {
            visibility = if (controlsVisible) VISIBLE else INVISIBLE
            setOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
        }

        binding.root.setOnClickListener {
            onClick()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        tickSound?.stop()
        handler.removeCallbacks(timerTask)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        applySettings()
    }

    private fun onTimer() {
        val time = Date()
        binding.content.run {
            hoursView.text = hoursFormat.format(time)
            minutesView.text = minutesFormat.format(time)
            secondsView.text = secondsFormat.format(time)
            amPmMarker.text = amPmFormat.format(time)
            dateView.text = dateFormat.format(time)
        }

        blinkTimeSeparator(time)
        playTickSound(time)
    }

    fun setControlsVisible(visible: Boolean) {
        controlsVisible = visible
        binding.settingsButton.apply {
            if (controlsVisible) {
                showAnimated(R.anim.fab_show, 0)
            } else {
                hideAnimated(R.anim.fab_hide, 0)
            }
        }
    }

    private fun isOddSecond(time: Date) = time.time / 1000 % 2 != 0L

    private fun applySettings() {
        val locale = Locale.getDefault()
        dateFormat = SimpleDateFormat(settings.getString(PREF_DATE_FORMAT), locale)

        binding.content.run {
            timeSeparator.visibility = VISIBLE

            if (settings.getBoolean(PREF_SECONDS_VISIBLE)) {
                secondsView.visibility = VISIBLE
                secondsSeparator.visibility = VISIBLE
            } else {
                secondsSeparator.visibility = GONE
                secondsView.visibility = GONE
            }

            if (settings.getBoolean(PREF_24_HOURS_FORMAT)) {
                hoursFormat = SimpleDateFormat("HH", locale)
                amPmMarker.visibility = GONE
            } else {
                hoursFormat = SimpleDateFormat("h", locale)
                amPmMarker.visibility = VISIBLE
            }

            if (settings.getBoolean(PREF_DATE_VISIBLE)) {
                dateView.visibility = VISIBLE
            } else {
                dateView.visibility = GONE
            }
        }

        setUpTickSound()
    }

    private fun setUpTickSound() {
        tickSound?.run {
            stop()
            release()
        }

        tickSound = null

        settings.getString(PREF_TICK_SOUND, null)?.let {
            val resId = resources.getIdentifier(it, "raw", requireContext().packageName)
            if (resId != 0) {
                tickSound = MediaPlayer.create(requireContext(), resId)
            }
        }
    }

    private fun playTickSound(time: Date) {
        tickSound?.run {
            seekTo(0)
            start()
        }
    }

    private fun blinkTimeSeparator(time: Date) {
        if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)) {
            val secondsVisible = settings.getBoolean(PREF_SECONDS_VISIBLE)
            binding.content.apply {
                if (isOddSecond(time)) {
                    timeSeparator.showAnimated(R.anim.time_separator_show, 0)
                    if (secondsVisible) {
                        secondsSeparator.showAnimated(R.anim.time_separator_show, 0)
                    }
                } else {
                    timeSeparator.hideAnimated(R.anim.time_separator_hide, 0)
                    if (secondsVisible) {
                        secondsSeparator.hideAnimated(R.anim.time_separator_hide, 0)
                    }
                }
            }
        }
    }

}