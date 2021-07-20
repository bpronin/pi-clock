package com.bopr.piclock

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.DEFAULT_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.Settings.Companion.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import java.text.DateFormat
import java.util.*

/**
 * Controls text date view (if exists) in analog clock.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockTextDateControl(private val view: View, settings: Settings) :
    ContentControlAdapter(settings) {

    private val dateView: AnimatedTextView? by lazy { view.findViewById(R.id.date_view) }

    private lateinit var dateFormat: DateFormat

    private var animated = true

    init {
        updateView()
        updateAnimated()
        updateViewData(Date())
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateView() {
        dateView?.apply {
            val pattern = settings.getString(PREF_DATE_FORMAT)
            dateFormat = if (pattern == SYSTEM_DEFAULT)
                DEFAULT_DATE_FORMAT
            else
                defaultDatetimeFormat(pattern)
            isGone = pattern.isEmpty()
        }
    }

    private fun updateViewData(time: Date) {
        dateView?.apply {
            if (isVisible) {
                setText(dateFormat.format(time), animated)
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick == 1) updateViewData(time)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimated()
            PREF_DATE_FORMAT -> updateView()
        }
    }

}