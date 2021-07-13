package com.bopr.piclock

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_SECOND_HAND_VISIBLE
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.setGone
import java.text.DateFormat
import java.util.*
import java.util.Calendar.*

/**
 * Convenience class to control digital clock representation of the content view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockControl(private val view: View, settings: Settings) :
    ContentControl(settings) {

// todo: stub. implement class

    //    private val amPmMarkerView: TextView = view.findViewById(R.id.am_pm_marker_view)
    //    private val amPmFormat = defaultDatetimeFormat("a")

    private val hourHandView: ImageView = view.findViewById(R.id.hour_hand_view)
    private val secondHandView: ImageView = view.findViewById(R.id.second_hand_view)
    private val minuteHandView: ImageView = view.findViewById(R.id.minute_hand_view)
    private val dateView: AnimatedTextView = view.findViewById(R.id.date_view)

    private lateinit var dateFormat: DateFormat

    private var animated: Boolean = true

    init {
        updateDateView()
        updateAnimated()
        updateSecondHandView()
        updateViewsData(Date(), false)
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateDateView() {
        val pattern = settings.getString(PREF_DATE_FORMAT)
        dateFormat = if (pattern == SYSTEM_DEFAULT)
            DEFAULT_DATE_FORMAT
        else
            defaultDatetimeFormat(pattern)
        dateView.setGone(pattern.isEmpty())
    }

    private fun updateViewsData(time: Date, animated: Boolean) {
        GregorianCalendar.getInstance().apply {
            this.time = time
            minuteHandView.rotation = get(MINUTE) * 6f
            hourHandView.rotation = get(HOUR) * 30f
            if (secondHandView.isVisible) {
                secondHandView.rotation = get(SECOND) * 6f
            }
        }

        if (dateView.isVisible) {
            dateView.setText(dateFormat.format(time), animated)
        }
    }

    private fun updateSecondHandView() {
        secondHandView.setGone(!settings.getBoolean(PREF_SECOND_HAND_VISIBLE))
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick % 2 == 0) {
            updateViewsData(time, animated)
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_SECOND_HAND_VISIBLE ->
                updateSecondHandView()
            PREF_ANIMATION_ON ->
                updateAnimated()
            PREF_DATE_FORMAT ->
                updateDateView()
        }
    }

    companion object {

        fun isAnalogClockLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_analog_")
        }
    }
}