package com.bopr.piclock

import android.view.View
import android.view.ViewGroup.*
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_DIGITS_SPLIT_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_HOURS_MINUTES_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECONDS_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_BLINKING
import com.bopr.piclock.Settings.Companion.PREF_TIME_SEPARATORS_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.getAnimatorResId
import com.bopr.piclock.util.ui.AnimatedTextView
import com.bopr.piclock.util.ui.ExtTextView
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
    private val hoursView: SplitAnimatedTextView by lazy { view.findViewById(R.id.hours_view) }
    private val minutesView: SplitAnimatedTextView by lazy { view.findViewById(R.id.minutes_view) }
    private val secondsView: SplitAnimatedTextView by lazy { view.findViewById(R.id.seconds_view) }
    private val dateView: AnimatedTextView by lazy { view.findViewById(R.id.date_view) }
    private val minutesSeparatorView: ExtTextView by lazy { view.findViewById(R.id.minutes_separator) }
    private val secondsSeparatorView: ExtTextView by lazy { view.findViewById(R.id.seconds_separator) }
    private val amPmMarkerView: ExtTextView by lazy { view.findViewById(R.id.am_pm_view) }

    private val blinker by lazy {
        TimeSeparatorBlinker(minutesSeparatorView, secondsSeparatorView).apply {
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
        updateDigitsSplitAnimation()
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
            minutesSeparatorView.visibility = VISIBLE
            secondsSeparatorView.isInvisible = !secondsVisible

            blinker.setEnabled(settings.getBoolean(PREF_TIME_SEPARATORS_BLINKING))
            blinker.setSecondsEnabled(secondsVisible)
        } else {
            minutesSeparatorView.visibility = INVISIBLE
            secondsSeparatorView.visibility = INVISIBLE

            blinker.setEnabled(false)
        }
    }

    private fun updateDigitsAnimation() {
        val resId = getAnimatorResId(settings.getString(PREF_DIGITS_ANIMATION))
        /* resId = 0 is allowed. it means that animation is disabled */
        hoursView.setTextAnimator(resId)
        minutesView.setTextAnimator(resId)
        secondsView.setTextAnimator(resId)
        dateView.setTextAnimator(resId)
    }

    private fun updateDigitsSplitAnimation() {
        val splitEnabled = settings.getBoolean(PREF_DIGITS_SPLIT_ANIMATION)
        hoursView.splitDigits = splitEnabled
        minutesView.splitDigits = splitEnabled
        secondsView.splitDigits = splitEnabled
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
            PREF_ANIMATION_ON -> updateAnimationOn()
            PREF_HOURS_MINUTES_FORMAT -> updateHoursMinutesViews()
            PREF_TIME_SEPARATORS_VISIBLE -> updateSeparatorsViews()
            PREF_TIME_SEPARATORS_BLINKING -> updateSeparatorsViews()
            PREF_DATE_FORMAT -> updateDateView()
            PREF_DIGITS_ANIMATION -> updateDigitsAnimation()
            PREF_DIGITS_SPLIT_ANIMATION -> updateDigitsSplitAnimation()
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