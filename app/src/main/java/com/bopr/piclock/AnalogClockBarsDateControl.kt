package com.bopr.piclock

import android.view.View
import android.widget.ImageView
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import java.util.*
import java.util.Calendar.DAY_OF_WEEK

/**
 * Controls bars date view (if exists) in analog clock.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockBarsDateControl(private val view: View, settings: Settings) :
    ContentControlAdapter(settings) {

    private val dateView: View? by lazy { view.findViewById(R.id.bars_date_view) }
    private val weekViews by lazy {
        arrayOf<ImageView>(
            view.findViewById(R.id.sun_view),
            view.findViewById(R.id.mon_view),
            view.findViewById(R.id.tue_view),
            view.findViewById(R.id.wed_view),
            view.findViewById(R.id.thu_view),
            view.findViewById(R.id.fri_view),
            view.findViewById(R.id.sat_view)
        )
    }

    private var animated: Boolean = true

    init {
        updateAnimated()
        updateViewData(Date())
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
    }

    private fun updateViewData(time: Date) {
        dateView?.apply {
            val today = GregorianCalendar.getInstance().run {
                this.time = time
                get(DAY_OF_WEEK) - 1
            }

            weekViews.forEachIndexed { index, view ->
                if (index == today) view.scaleY = 1f else view.scaleY = 0.5f
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick == 1) updateViewData(time)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimated()
        }
    }
}