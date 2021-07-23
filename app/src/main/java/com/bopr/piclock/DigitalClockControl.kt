package com.bopr.piclock

import android.view.View
import android.view.ViewGroup.*
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_HOURS_MINUTES_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.getResId
import com.bopr.piclock.util.ui.AnimatedTextView
import com.bopr.piclock.util.ui.SplitAnimatedTextView
import java.text.DateFormat
import java.util.*

/**
 * Controls digital clock representation of the content view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class DigitalClockControl(view: View, settings: Settings) :
    ContentControlAdapter(settings) {
    //todo: time separators : - . | * ~
    private val amPmFormat = defaultDatetimeFormat("a")
    private val hoursView: AnimatedTextView by lazy { view.findViewById(R.id.hours_view) }
    private val minutesView: AnimatedTextView by lazy { view.findViewById(R.id.minutes_view) }
    private val secondsView: SplitAnimatedTextView by lazy { view.findViewById(R.id.seconds_view) }
    private val dateView: AnimatedTextView by lazy { view.findViewById(R.id.date_view) }
    private val minutesSeparator: TextView by lazy { view.findViewById(R.id.minutes_separator) }
    private val secondsSeparator: TextView by lazy { view.findViewById(R.id.seconds_separator) }
    private val amPmMarkerView: TextView = view.findViewById(R.id.am_pm_view)

    private val blinker by lazy {
        TimeSeparatorBlinker(minutesSeparator, secondsSeparator).apply {
            setEnabled(
                settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE) &&
                        settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING)
            )
            setSecondsEnabled(settings.getString(PREF_SECONDS_FORMAT).isNotEmpty())
            setAnimated(animationOn)
        }
    }

    private lateinit var hoursFormat: DateFormat
    private lateinit var minutesFormat: DateFormat
    private lateinit var secondsFormat: DateFormat
    private lateinit var dateFormat: DateFormat

    private var animationOn: Boolean = true
    private var currentTime = Date()

    init {
        updateHoursMinutesViews()
        updateSecondsView()
        updateSeparatorsViews()
        updateDateView()
        updateDigitsAnimation()
        updateAnimationOn()
        updateViewsData(false)
    }

    private fun updateAnimationOn() {
        animationOn = settings.getBoolean(PREF_ANIMATION_ON)
        blinker.setAnimated(animationOn)
    }

    private fun updateHoursMinutesViews() {
        val patterns = settings.getString(PREF_HOURS_MINUTES_FORMAT).split(":")
        val hoursPattern = patterns[0]
        val minutesPattern = patterns[1]

        hoursFormat = defaultDatetimeFormat(hoursPattern)
        minutesFormat = defaultDatetimeFormat(minutesPattern)
        amPmMarkerView.isGone = !hoursPattern.startsWith("h")
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

        dateFormat = if (pattern == SYSTEM_DEFAULT)
            DEFAULT_DATE_FORMAT
        else
            defaultDatetimeFormat(pattern)

        dateView.isGone = pattern.isEmpty()
        if (dateView.isVisible) {
            updateViewsData(animationOn)
        }
    }

    private fun updateSeparatorsViews() {
        if (settings.getBoolean(PREF_TIME_SEPARATORS_VISIBLE)) {
            val secondsVisible = settings.getString(PREF_SECONDS_FORMAT).isNotEmpty()
            minutesSeparator.visibility = VISIBLE
            secondsSeparator.isGone = !secondsVisible

            blinker.setEnabled(settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING))
            blinker.setSecondsEnabled(secondsVisible)
        } else {
            minutesSeparator.visibility = INVISIBLE
            secondsSeparator.visibility = INVISIBLE

            blinker.setEnabled(false)
        }
    }

    private fun updateDigitsAnimation() {
        val resId = getResId("animator", settings.getString(PREF_DIGITS_ANIMATION))
        if (resId != 0) { /* 0 = animation disabled for digits */
            hoursView.setTextAnimator(resId)
            minutesView.setTextAnimator(resId)
            secondsView.setTextAnimator(resId)
            dateView.setTextAnimator(resId)
        }
    }

    private fun updateViewsData(animated: Boolean) {
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

    override fun onTimer(time: Date, tick: Int) {
        currentTime = time
        if (tick == 1) updateViewsData(animationOn)
        blinker.onTimer(currentTime, tick)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON ->
                updateAnimationOn()
            PREF_HOURS_MINUTES_FORMAT ->
                updateHoursMinutesViews()
            PREF_TIME_SEPARATORS_VISIBLE ->
                updateSeparatorsViews()
            PREF_TIME_SEPARATORS_BLINKING ->
                updateSeparatorsViews()
            PREF_DATE_FORMAT ->
                updateDateView()
            PREF_DIGITS_ANIMATION ->
                updateDigitsAnimation()
            PREF_SECONDS_FORMAT -> {
                updateSecondsView()
                updateSeparatorsViews()
            }
        }
    }

    companion object {

        fun isDigitalClockLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_digital_")
        }
    }
}