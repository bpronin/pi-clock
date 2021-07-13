package com.bopr.piclock

import android.view.View
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import java.text.DateFormat
import java.util.*

/**
 * Convenience class to control digital clock representation of the content view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockControl(private val view: View, settings: Settings) :
    ContentControl(settings) {

// todo: stub. implement class

//    private val amPmFormat = defaultDatetimeFormat("a")
//    private val dateView: AnimatedTextView = view.findViewById(R.id.date_view)
//    private val amPmMarkerView: TextView = view.findViewById(R.id.am_pm_marker_view)

    private lateinit var dateFormat: DateFormat

    private var animated: Boolean = true

    init {
        updateDateView()
        updateAnimated()
        updateViewsData(Date(), false)
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateDateView() {
//        val pattern = settings.getString(PREF_DATE_FORMAT)
//
//        dateFormat =
//            if (pattern == SYSTEM_DEFAULT) DEFAULT_DATE_FORMAT else defaultDatetimeFormat(
//                pattern
//            )
//        dateView.visibility = if (pattern.isEmpty()) GONE else VISIBLE
    }

    private fun updateViewsData(time: Date, animated: Boolean) {
//        if (dateView.visibility == VISIBLE) {
//            dateView.setText(dateFormat.format(time), animated)
//        }
//        if (amPmMarkerView.visibility == VISIBLE) {
//            amPmMarkerView.text = amPmFormat.format(time)
//        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick % 2 == 0) {
            updateViewsData(time, animated)
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
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