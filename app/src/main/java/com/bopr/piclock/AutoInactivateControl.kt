package com.bopr.piclock

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.Settings.Companion.PREF_AUTO_INACTIVATE_DELAY

/**
 * Controls auto-switching app into inactive mode after delay.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AutoInactivateControl(
    private val handler: Handler,
    settings: Settings
) : ContentControlAdapter(settings) {

    private val task = Runnable {
        if (enabled) {
            Log.d(TAG, "Inactivating")

            onInactivate()
        }
    }

    private var enabled = false
        set(value) {
            if (field != value && delay > 0) {
                field = value
                if (field) {
                    Log.d(TAG, "Enabled")

                    handler.postDelayed(task, delay)
                } else {
                    Log.d(TAG, "Disabled")

                    handler.removeCallbacks(task)
                }
            }
        }
    private var delay = 0L

    lateinit var onInactivate: () -> Unit

    init {
        updateDelay()
    }

    private fun updateDelay() {
        delay = settings.getLong(PREF_AUTO_INACTIVATE_DELAY)
    }

    override fun onSettingChanged(key: String) {
        if (key == PREF_AUTO_INACTIVATE_DELAY) updateDelay()
    }

    override fun onModeChanged(animate: Boolean) {
        resume()
    }

    fun onTouch(event: MotionEvent): Boolean {
//        Log.v(TAG, "Processing touch: ${event.action}")

        when (event.action) {
            ACTION_DOWN -> pause()
            ACTION_UP -> resume()
        }
        return false
    }

    fun pause() {
        Log.d(TAG, "Pause")

        enabled = false
    }

    fun resume() {
        Log.d(TAG, "Resume")

        enabled = (mode == MODE_ACTIVE)
    }

    companion object {

        private const val TAG = "AutoInactivateControl"
    }
}
