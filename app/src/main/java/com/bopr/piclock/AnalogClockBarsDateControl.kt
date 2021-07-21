package com.bopr.piclock

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.getColor
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_WEEK_START
import com.bopr.piclock.util.dayOfWeek
import com.bopr.piclock.util.forEachChildIndexed
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

    private var animated = true
    private var currentTime = Date()

    init {
        updateAnimated()
        updateView()
    }

    private fun dayViewIndex(dayOfWeek: Int, firstDay: Int): Int {
        val k = dayOfWeek - firstDay
        return if (k >= 0) k else k + 7
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateView() {
        dateView?.apply {
            val firstDay = settings.getInt(PREF_WEEK_START)
            val sundayIndex = dayViewIndex(SUNDAY, firstDay)
            val dayIndex = dayViewIndex(dayOfWeek(currentTime), firstDay)
            forEachChildIndexed<ImageView> { index, view ->
                view.apply {
                    val image = if (index == dayIndex)
                        R.drawable.view_bar_date_bar_today
                    else
                        R.drawable.view_bar_date_bar

                    val color = if (index == sundayIndex)
                        R.color.orange
                    else
                        R.color.white
                    setImageResource(image)
                    setColorFilter(getColor(view.context, color))
                }
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick == 1) {
            currentTime = time
            updateView()
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimated()
            PREF_WEEK_START -> updateView()
        }
    }
}