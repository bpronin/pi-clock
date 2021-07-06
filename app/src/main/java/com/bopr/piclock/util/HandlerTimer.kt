package com.bopr.piclock.util

import android.os.Handler
import android.util.Log

class HandlerTimer(
    private val handler: Handler,
    private val interval: Long,
    private val onTimer: () -> Unit,
    private val onStart: () -> Unit = {},
    private val onEnd: () -> Unit = {}
) {

    private val _tag = "HandlerTimer"

    private val task = Runnable(this::executeTask)

    var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Started")

                    handler.post(task)
                    onStart()
                } else {
                    Log.d(_tag, "Stopped")

                    handler.removeCallbacks(task)
                    onEnd()
                }
            }
        }

    private fun executeTask() {
        onTimer()
        handler.postDelayed(task, interval)
    }

}

//fun isWholeSecond(time: Date) = time.time / 500 % 2 == 0L
//
//fun isOddSecond(time: Date) = time.time / 500 % 4 == 0L
