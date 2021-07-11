package com.bopr.piclock

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY

/**
 * Convenience class for auto switching app into inactive mode after delay.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AutoInactivateControl(
    private val handler: Handler,
    private val settings: Settings
) {

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

    @Mode
    private var mode = MODE_INACTIVE
    private var delay = 0L

    lateinit var onInactivate: () -> Unit

    init {
        delay = settings.getLong(PREF_AUTO_INACTIVATE_DELAY)
    }

    fun onSettingChanged(key: String) {
        if (key == PREF_AUTO_INACTIVATE_DELAY) {
            delay = settings.getLong(key)
        }
    }

    fun onModeChanged(@Mode value: Int) {
        mode = value
        resume()
    }

    fun onTouch(event: MotionEvent): Boolean {
//        Log.v(_tag, "Processing touch: ${event.action}")

        when (event.action) {
            ACTION_DOWN -> pause()
            ACTION_UP -> resume()
        }
        return false
    }

    fun pause() {
        Log.d(_tag, "Pause")

        enabled = false
    }

    fun resume() {
        Log.d(_tag, "Resume")

        enabled = (mode == MODE_ACTIVE)
    }

}
