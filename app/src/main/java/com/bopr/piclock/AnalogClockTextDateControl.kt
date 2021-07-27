package com.bopr.piclock

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_DATE_FORMAT
import com.bopr.piclock.util.DEFAULT_DATE_FORMAT
import com.bopr.piclock.util.SYSTEM_DEFAULT
import com.bopr.piclock.util.defaultDatetimeFormat
import com.bopr.piclock.util.ui.AnimatedTextView
import java.text.DateFormat
import java.util.*

/**
 * Controls analog clock view with text date view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockTextDateControl(private val view: View, settings: Settings) :
    AnalogClockControl(view, settings) {

    private val dateView: AnimatedTextView by lazy { view.findViewById(R.id.date_view) }

    private lateinit var dateFormat: DateFormat

    private var animationOn = true
    private var currentTime = Date()

    init {
        updateAnimationOn()
        updateView()
    }

    private fun updateAnimationOn() {
        animationOn = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateView() {
        dateView.apply {
            val pattern = settings.getString(PREF_DATE_FORMAT)
            dateFormat = if (pattern == SYSTEM_DEFAULT)
                DEFAULT_DATE_FORMAT
            else
                defaultDatetimeFormat(pattern)
            isGone = pattern.isEmpty()

            if (isVisible) {
                setText(dateFormat.format(currentTime), animationOn)
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        super.onTimer(time, tick)
        if (tick == 1) {
            currentTime = time
            updateView()
        }
    }

    override fun onSettingChanged(key: String) {
        super.onSettingChanged(key)
        when (key) {
            PREF_ANIMATION_ON -> updateAnimationOn()
            PREF_DATE_FORMAT -> updateView()
        }
    }

}