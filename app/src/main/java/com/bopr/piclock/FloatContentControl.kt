package com.bopr.piclock

import android.os.Handler
import android.util.Log
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE

/**
 * Controls floating content along the screen.
 */
internal class FloatContentControl(private val handler: Handler) {

    private val _tag = "FloatContentControl"

    private val task = Runnable { floatSomewhere() }

    private var interval = 0L

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
                    floatHome()
                }
            }
        }

    var floating = false
        private set(value) {
            if (field != value) {
                field = value
                onFloating(field)
            }
        }

    lateinit var onFloatSomewhere: (onEnd: () -> Unit) -> Unit
    lateinit var onFloatHome: (onEnd: () -> Unit) -> Unit
    lateinit var onFloating: (floating: Boolean) -> Unit

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
                    Log.v(_tag, "Floating task not scheduled")
                }
            }
        }
    }

    private fun floatSomewhere() {
        Log.d(_tag, "Floating somewhere")

        floating = true
        onFloatSomewhere {
            Log.v(_tag, "End floating animation")

            floating = false
            scheduleFloatContent()
        }
    }

    private fun floatHome() {
        Log.d(_tag, "Floating home")

        floating = true
        onFloatHome {
            Log.v(_tag, "End floating animation")

            floating = false
        }
    }

    fun setInterval(interval: Long) {
        this.interval = interval
    }

    fun onChangeViewMode(mode: Int) {
        enabled = (mode == MODE_INACTIVE)
    }

}
