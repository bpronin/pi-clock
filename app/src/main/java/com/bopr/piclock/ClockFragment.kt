package com.bopr.piclock

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MINUTES_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATOR_BLINKING
import com.bopr.piclock.databinding.FragmentClockBinding
import com.bopr.piclock.util.hideAnimated
import com.bopr.piclock.util.showAnimated
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ClockFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    private lateinit var settings: Settings
    private lateinit var binding: FragmentClockBinding

    private val handler = Handler(Looper.getMainLooper())
    private var hoursFormat24 = SimpleDateFormat("HH", Locale.getDefault())
    private var hoursFormatAmPm = SimpleDateFormat("h", Locale.getDefault())
    private var amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private var minutesFormat = SimpleDateFormat("mm", Locale.getDefault())
    private var secondsFormat = SimpleDateFormat("ss", Locale.getDefault())
    private var dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
    private var controlsVisible = false

    private val timerTask = object : Runnable {

        override fun run() {
            updateTimeViews()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
        settings.loadDefaults()
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClockBinding.inflate(layoutInflater)
        applySettings()

        binding.settingsButton.apply {
            visibility = if (controlsVisible) VISIBLE else INVISIBLE
            setOnClickListener {
                showSettings()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTask)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        applySettings()
    }

    fun showControls(visible: Boolean) {
        controlsVisible = visible
        binding.settingsButton.apply {
            if (controlsVisible) {
                showAnimated(R.anim.fab_show, 200)
            } else {
                hideAnimated(R.anim.fab_hide, 200)
            }
        }
    }

    private fun updateTimeViews() {
        val time = Date()
        binding.run {
            hoursView.text = getHoursFormat().format(time)
            minutesView.text = minutesFormat.format(time)
            secondsView.text = secondsFormat.format(time)
            amPmMarker.text = amPmFormat.format(time)
            dateView.text = dateFormat.format(time)
            minutesSeparator.visibility = getMinutesSeparatorVisibility(time)
        }
    }

    private fun applySettings() {
        binding.run {
            minutesSeparator.text = settings.getString(PREF_MINUTES_SEPARATOR)
            secondsSeparator.visibility = getSecondsVisibility()
            secondsSeparator.text = settings.getString(PREF_SECONDS_SEPARATOR)
            secondsView.visibility = getSecondsVisibility()
            amPmMarker.visibility = getAmPmMarkerVisibility()
        }
    }

    private fun getHoursFormat(): DateFormat {
        return if (settings.getBoolean(PREF_24_HOURS_FORMAT)) hoursFormat24 else hoursFormatAmPm
    }

    private fun getAmPmMarkerVisibility(): Int {
        return if (settings.getBoolean(PREF_24_HOURS_FORMAT)) INVISIBLE else VISIBLE
    }

    private fun getMinutesSeparatorVisibility(time: Date): Int {
        return if (settings.getBoolean(PREF_TIME_SEPARATOR_BLINKING)
            && (time.time / 1000 % 2 != 0L)
        ) INVISIBLE else VISIBLE
    }

    private fun getSecondsVisibility(): Int {
        return if (settings.getBoolean(PREF_SECONDS_VISIBLE)) VISIBLE else GONE
    }

    private fun showSettings() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

}