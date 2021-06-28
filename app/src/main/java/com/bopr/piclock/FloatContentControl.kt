package com.bopr.piclock

import android.os.Handler
import android.util.Log
//todo:implement
internal class FloatContentControl(private val handler: Handler) {

    /** Logger tag. */
    private val _tag = "FloatContentControl"

    lateinit var onFloatingChanged: (floating: Boolean) -> Unit
    lateinit var onFloat: (onEnd: () -> Unit) -> Unit
    var interval: Long = 0L
    var floating = false
        private set(value) {
            if (field != value) {
                field = value
                onFloatingChanged(field)
            }
        }
    var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Floating enabled")

                    scheduleFloatContent()
                } else {
                    Log.d(_tag, "Floating disabled")

                    handler.removeCallbacks(task)
                }
            }
        }

    private val task = Runnable { floatContent() }

    private fun scheduleFloatContent() {
        if (enabled) {
            when {
                interval == 0L -> {
                    handler.post(task)

                    Log.d(_tag, "Floating task posted now")
                }
                interval > 0 -> {
                    handler.postDelayed(task, interval)

                    Log.d(_tag, "Floating task scheduled after: $interval ms")
                }
                else -> {
                    throw IllegalArgumentException("Invalid interval: $interval")
                }
            }
        }
    }

    private fun floatContent() {
        Log.d(_tag, "Start floating animation")

        floating = true
        onFloat {
            Log.d(_tag, "End floating animation")

            floating = false
            scheduleFloatContent()
        }
    }


}
