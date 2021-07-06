package com.bopr.piclock

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Mode

/**
 * Responsible for auto-deactivation of [MainFragment].
 */
internal class AutoDeactivationControl(private val handler: Handler) {

    private val _tag = "AutoDeactivationControl"

    var delay = 0L
    lateinit var onDeactivate: () -> Unit

    private val task = Runnable {
        if (enabled) {
            Log.d(_tag, "Deactivating")

            onDeactivate()
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

    fun onModeChanged(@Mode mode: Int) {
        enabled = (mode == MODE_ACTIVE)
    }

    fun onPause() {
        Log.v(_tag, "Pause")

        enabled = false
    }

    fun onResume() {
        Log.v(_tag, "Resume")

        enabled = true
    }

    fun onTouch(event: MotionEvent, @Mode mode: Int): Boolean {
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
