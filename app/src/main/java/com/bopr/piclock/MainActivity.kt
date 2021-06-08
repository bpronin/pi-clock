package com.bopr.piclock

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.bopr.piclock.Settings.Companion.PREF_24_HOURS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_MINUTES_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_MINUTES_SEPARATOR_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_SEPARATOR
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_VISIBLE
import com.bopr.piclock.databinding.ActivityMainBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fullscreenSupport: FullscreenSupport
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnClickListener { onScreenTouch() }

        fullscreenSupport = FullscreenSupport(window)

        settings = Settings(this)
        settings.loadDefaults()
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls
         are available. */
        fullscreenSupport.fullscreen = true
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTask)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        TODO("Not yet implemented")
    }

    private fun onScreenTouch() {
        fullscreenSupport.fullscreen = !fullscreenSupport.fullscreen
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