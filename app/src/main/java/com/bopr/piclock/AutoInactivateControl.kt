package com.bopr.piclock

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode

/**
 * Responsible for auto switching [MainFragment] to inactive mode.
 */
internal class AutoInactivateControl(private val handler: Handler) {

    private val _tag = "AutoInactivateControl"

    private val task = Runnable {
        if (enabled) {
            Log.d(_tag, "Inactivating")

            onInactivate()
        }
    }

    private var enabled = false
        set(value) {
            if (field != value && delay > 0) {
                field = value
                if (field) {
                    Log.d(_tag, "Enabled")

                    handler.postDelayed(task, delay)
                } else {
                    Log.d(_tag, "Disabled")

                    handler.removeCallbacks(task)
                }
            }
        }
    lateinit var onInactivate: () -> Unit

    @Mode
    var mode = MODE_INACTIVE
    var delay = 0L

    fun onModeChanged(@Mode mode: Int) {
        this.mode = mode
        enabled = (this.mode == MODE_ACTIVE)
    }

    fun onPause() {
        Log.v(_tag, "Pause")

        enabled = false
    }

    fun onResume() {
        Log.v(_tag, "Resume")

        enabled = true
    }

    fun onTouch(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> {
                Log.v(_tag, "Processing touch: ${event.action}")

                enabled = false
            }
            ACTION_UP -> {
                Log.v(_tag, "Processing touch: ${event.action}")

                onModeChanged(mode)
            }
        }
        return false
    }

}
