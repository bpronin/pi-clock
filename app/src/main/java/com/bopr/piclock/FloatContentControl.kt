package com.bopr.piclock

import android.os.Handler
import android.util.Log
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE

/**
 * Controls floating content along the screen.
 */
internal class FloatContentControl(private val handler: Handler) {

    private val _tag = "FloatContentControl"

    var interval = 0L
        set(value) {
            if (field != value) {
                field = value

                Log.d(_tag, "Interval set to: $interval")
            }
        }
    var busy = false
        private set(value) {
            if (field != value) {
                field = value
                onBusy(field)
            }
        }
    lateinit var onFloatSomewhere: (onEnd: () -> Unit) -> Unit
    lateinit var onFloatHome: (onEnd: () -> Unit) -> Unit
    lateinit var onBusy: (busy: Boolean) -> Unit

    private val task = Runnable { floatSomewhere() }
    private var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Enabled")

                    scheduleTask()
                } else {
                    Log.d(_tag, "Disabled")

                    handler.removeCallbacks(task)
                    floatHome()
                }
            }
        }

    private fun scheduleTask() {
        if (enabled) {
            when {
                interval == 0L -> {
                    handler.post(task)

                    Log.d(_tag, "Task posted now")
                }
                interval > 0 -> {
                    handler.postDelayed(task, interval)

                    Log.d(_tag, "Task scheduled after: $interval")
                }
                else -> {
                    Log.v(_tag, "Task not scheduled. interval: $interval")
                }
            }
        }
    }

    private fun floatSomewhere() {
        Log.v(_tag, "Start floating somewhere")

        busy = true
        onFloatSomewhere {
            Log.v(_tag, "End floating somewhere")

            busy = false
            scheduleTask()
        }
    }

    private fun floatHome() {
        Log.v(_tag, "Start floating home")

        busy = true
        onFloatHome {
            Log.v(_tag, "End floating home")

            busy = false
        }
    }

    fun onModeChanged(mode: Int) {
        enabled = (mode == MODE_INACTIVE)
    }

    fun onPause() {
        Log.v(_tag, "Pause")

        enabled = false
    }

    fun onResume() {
        Log.v(_tag, "Resume")

        enabled = true
    }

}
