package com.bopr.piclock

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MINUTES_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_MINUTES_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.databinding.FragmentClockBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ClockFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    private lateinit var binding: FragmentClockBinding
    private lateinit var settings: Settings
    private val handler = Handler(Looper.getMainLooper())
    private var hoursFormat24 = SimpleDateFormat("HH", Locale.getDefault())
    private var hoursFormatAmPm = SimpleDateFormat("h", Locale.getDefault())
    private var amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    private var minutesFormat = SimpleDateFormat("mm", Locale.getDefault())
    private var secondsFormat = SimpleDateFormat("ss", Locale.getDefault())
    private var dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())

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
        TODO("Not yet implemented")
    }

    private fun updateTimeViews() {
        val time = Date()
        binding.run {
            hoursView.text = getHoursFormat().format(time)
            minutesView.text = minutesFormat.format(time)

            minutesSeparator.visibility = getMinutesSeparatorVisibility(time)
            minutesSeparator.text = settings.getString(PREF_MINUTES_SEPARATOR)

            secondsSeparator.text = settings.getString(PREF_SECONDS_SEPARATOR)

            secondsView.visibility = getSecondsVisibility()
            secondsView.text = secondsFormat.format(time)

            amPmMarker.visibility = getAmPmMarkerVisibility()
            amPmMarker.text = amPmFormat.format(time)

            dateView.text = dateFormat.format(time)
        }
    }

    private fun getHoursFormat(): DateFormat {
        return if (settings.getBoolean(PREF_24_HOURS_FORMAT)) hoursFormat24 else hoursFormatAmPm
    }

    private fun getAmPmMarkerVisibility(): Int {
        return if (settings.getBoolean(PREF_24_HOURS_FORMAT)) INVISIBLE else VISIBLE
    }

    private fun getMinutesSeparatorVisibility(time: Date): Int {
        return if (settings.getBoolean(PREF_MINUTES_SEPARATOR_BLINKING)
            && (time.time / 1000 % 2 == 0L)
        ) VISIBLE else INVISIBLE
    }

    private fun getSecondsVisibility(): Int {
        return if (settings.getBoolean(PREF_SECONDS_VISIBLE)) VISIBLE else INVISIBLE
    }

}