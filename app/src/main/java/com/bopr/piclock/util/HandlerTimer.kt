package com.bopr.piclock.util

import android.os.Handler

class HandlerTimer(
    private val handler: Handler,
    private val interval: Long,
    private val ticksCount: Int,
    private val onTimer: (tick: Int) -> Unit
) {

    private var tick = 1
    private val task = Runnable(::executeTask)

    var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    tick = 1
                    handler.post(task)
                } else {
                    handler.removeCallbacks(task)
                }
            }
        }

    private fun executeTask() {
        if (enabled) {
            onTimer(tick)
            if (tick < ticksCount) tick++ else tick = 1
            handler.postDelayed(task, interval)
        }
    }

}