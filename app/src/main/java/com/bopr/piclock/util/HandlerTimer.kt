package com.bopr.piclock.util

import android.os.Handler

class HandlerTimer(
    private val handler: Handler,
    private val interval: Long,
    private val onTimer: () -> Unit,
) {

    private val task = Runnable(::executeTask)

    var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    handler.post(task)
                } else {
                    handler.removeCallbacks(task)
                }
            }
        }

    private fun executeTask() {
        if (enabled) {
            onTimer()
            handler.postDelayed(task, interval)
        }
    }

}