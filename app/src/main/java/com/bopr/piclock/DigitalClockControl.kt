package com.bopr.piclock

import android.view.View
import android.view.ViewGroup.*
import android.widget.TextView
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.getResId
import java.text.DateFormat
import java.util.*

class DigitalClockControl(private val view: View, private val settings: Settings) : ContentControl {

    private val amPmFormat = defaultDatetimeFormat("a")
    private lateinit var hoursFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var dateFormat: DateFormat

    private val hoursView: AnimatedTextView = view.findViewById(R.id.hours_view)
    private val minutesView: AnimatedTextView = view.findViewById(R.id.minutes_view)
    private val secondsView: AnimatedTextView = view.findViewById(R.id.seconds_view)
    private val dateView: AnimatedTextView = view.findViewById(R.id.date_view)
    private val minutesSeparator: TextView = view.findViewById(R.id.minutes_separator)
    private val secondsSeparator: TextView = view.findViewById(R.id.seconds_separator)
    private val amPmMarkerView: TextView = view.findViewById(R.id.am_pm_marker_view)

    private val blinkAnimator by lazy {
        BlinkControl(minutesSeparator, secondsSeparator).apply {
            setEnabled(
                settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE) &&
                        settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)
            )
            setSecondsEnabled(settings.getString(PREF_SECONDS_FORMAT).isNotEmpty())
            setAnimated(animated)
        }
    }

    private var animated: Boolean = true

    init {
        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorsViews()
        updateDateView()
        updateDigitsAnimation()
    }

    override fun setAnimated(value: Boolean) {
        animated = value
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick % 2 == 0) {
            hoursView.setText(hoursFormat.format(time), animated)
            minutesView.setText(minutesFormat.format(time), animated)
            if (dateView.visibility == VISIBLE) {
                dateView.setText(dateFormat.format(time), animated)
            }
            if (amPmMarkerView.visibility == VISIBLE) {
                amPmMarkerView.text = amPmFormat.format(time)
            }
            if (secondsView.visibility == VISIBLE) {
                secondsView.setText(secondsFormat.format(time), animated)
            }
        }

        blinkAnimator.onTimer(tick)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
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
        }
    }

    private fun updateHoursMinutesViews() {
        val patterns = settings.getString(PREF_TIME_FORMAT).split(":")
        val hoursPattern = patterns[0]
        val minutesPattern = patterns[1]

        hoursFormat = defaultDatetimeFormat(hoursPattern)
        minutesFormat = defaultDatetimeFormat(minutesPattern)
        amPmMarkerView.visibility =
            if (hoursPattern.startsWith("h")) VISIBLE else GONE
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
            if (pattern == SYSTEM_DEFAULT) DEFAULT_DATE_FORMAT else defaultDatetimeFormat(
                pattern
            )
        dateView.visibility = if (pattern.isEmpty()) GONE else VISIBLE
    }

    private fun updateSeparatorsViews() {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)) {
            val secondsVisible = settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()
            minutesSeparator.visibility = VISIBLE
            secondsSeparator.visibility =
                if (secondsVisible) VISIBLE else INVISIBLE

            blinkAnimator.setEnabled(settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING))
            blinkAnimator.setSecondsEnabled(secondsVisible)
        } else {
            minutesSeparator.visibility = INVISIBLE
            secondsSeparator.visibility = INVISIBLE

            blinkAnimator.setEnabled(false)
        }
    }

    private fun updateDigitsAnimation() {
        val resId = view.context.getResId("animator", settings.getString(PREF_DIGITS_ANIMATION))

        hoursView.setTextAnimator(resId)
        minutesView.setTextAnimator(resId)
        secondsView.setTextAnimator(resId)
        dateView.setTextAnimator(resId)
    }

    companion object {

        fun isDigitalClockLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_digital_")
        }
    }
}