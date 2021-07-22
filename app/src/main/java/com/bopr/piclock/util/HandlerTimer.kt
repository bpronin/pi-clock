package com.bopr.piclock.util

import android.os.Handler

/**
 * [Handler] based timer.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HandlerTimer(
    private val handler: Handler,
    var interval: Long,
    private val onTimer: () -> Unit
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