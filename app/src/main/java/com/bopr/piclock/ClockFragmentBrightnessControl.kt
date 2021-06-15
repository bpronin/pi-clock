package com.bopr.piclock

import android.content.Context
import android.view.View

internal class ClockFragmentBrightnessControl(
    context: Context,
    controllerView: View,
    private val controllingView: View
) {

    var minBrightness: Int = 0
    var maxBrightness: Int = 0
    var onEnd: () -> Unit = {}

    fun updateBrightness(active: Boolean) {
        controllingView.alpha = (if (active) maxBrightness else minBrightness) / 100f
    }

}
