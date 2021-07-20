package com.bopr.piclock

import android.view.View
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_SECOND_HAND_VISIBLE
import com.bopr.piclock.util.isOdd
import java.util.*
import java.util.Calendar.*

/**
 * Controls analog clock representation of the content view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockControl(private val view: View, settings: Settings) :
    ContentControlAdapter(settings) {

// todo:  часы наизнанку - стрелки прикреплены к ободу а не к центру

    private val hourHandView: ImageView by lazy { view.findViewById(R.id.hour_hand_view) }
    private val secondHandView: ImageView by lazy { view.findViewById(R.id.second_hand_view) }
    private val minuteHandView: ImageView by lazy { view.findViewById(R.id.minute_hand_view) }
    private val textDateViewControl = AnalogClockTextDateControl(view, settings)
    private val barsDateControl = AnalogClockBarsDateControl(view, settings)

    private var animated: Boolean = true

    init {
        updateAnimated()
        updateSecondHandView()
        updateViewData(Date(), 1)
    }

    private fun updateSecondHandView() {
        secondHandView.isGone = !settings.getBoolean(PREF_SECOND_HAND_VISIBLE)
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateViewData(time: Date, tick: Int) {
        GregorianCalendar.getInstance().apply {
            this.time = time
            if (tick == 1) {
                minuteHandView.rotation = get(MINUTE) * 6f
                hourHandView.rotation = get(HOUR) * 30f
            }
            if (tick.isOdd && secondHandView.isVisible) {
                secondHandView.rotation = get(SECOND) * 6f
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        updateViewData(time, tick)
        barsDateControl.onTimer(time, tick)
        textDateViewControl.onTimer(time, tick)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_SECOND_HAND_VISIBLE -> updateSecondHandView()
            PREF_ANIMATION_ON -> updateAnimated()
        }
        barsDateControl.onSettingChanged(key)
        textDateViewControl.onSettingChanged(key)
    }

    companion object {

        fun isAnalogClockLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_analog_")
        }
    }
}