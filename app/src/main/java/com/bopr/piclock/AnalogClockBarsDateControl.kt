package com.bopr.piclock

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.children
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_WEEK_START
import com.bopr.piclock.util.dayOfWeek
import java.util.*
import java.util.Calendar.SUNDAY

/**
 * Controls bars date view (if exists) in analog clock.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockBarsDateControl(private val view: View, settings: Settings) :
    ContentControlAdapter(settings) {

    //todo: idea: marks (radial red hatch) of appointments (some events)) on clock face

    private val dateView: ViewGroup? by lazy { view.findViewById(R.id.bars_date_view) }

    private var firstDay = 0
    private var animated = true

    init {
        updateView()
        updateAnimated()
        updateViewData(Date())
    }

    private fun dayViewIndex(dayOfWeek: Int): Int {
        return dayOfWeek - 1
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateView() {
        firstDay = settings.getInt(PREF_WEEK_START)

        dateView?.apply {
            val sundayIndex = dayViewIndex(SUNDAY)
            children.forEachIndexed { index, view ->
                val color = if (index == sundayIndex)
                    R.color.orange
                else
                    R.color.white
                (view as ImageView).setColorFilter(getColor(view.context, color))
            }
        }
    }

    private fun updateViewData(time: Date) {
        dateView?.apply {
            val todayIndex = dayViewIndex(dayOfWeek(time))
            children.forEachIndexed { index, view ->
                val image = if (index == todayIndex)
                    R.drawable.view_bar_date_bar_today
                else
                    R.drawable.view_bar_date_bar
                (view as ImageView).setImageResource(image)
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick == 1) updateViewData(time)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimated()
            PREF_WEEK_START -> updateView()
        }
    }
}