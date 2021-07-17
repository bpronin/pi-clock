package com.bopr.piclock

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View.*
import android.view.WindowInsets.Type.systemBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED

/**
 * Convenience class to control system UI's visibility.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class FullscreenControl(
    private val activity: Activity,
    private val handler: Handler,
    settings: Settings
) : ContentControlAdapter(settings) {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private val startDelay = 300L
    private val turnOnTask = Runnable {
        showSystemUI()
    }
    private val turnOffTask = Runnable {
        hideSystemUI()
    }

    private var fullscreen: Boolean = false
        set(value) {
            if (field != value) {
                handler.removeCallbacks(turnOnTask)
                handler.removeCallbacks(turnOffTask)
                field = value

                val task = if (field) turnOffTask else turnOnTask
                handler.postDelayed(task, startDelay)

                Log.d(TAG, "Fullscreen: $field")
            }
        }
    private var enabled = true
        set(value) {
            if (field != value) {
                field = value
                if (!field) {
                    fullscreen = false
                }

                Log.d(TAG, "Enabled: $field")
            }
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.apply {
                setDecorFitsSystemWindows(false)
                insetsController?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        updateEnabled()
    }

    private fun showSystemUI() {
        activity.window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.show(systemBars())
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
        }

        Log.d(TAG, "System UI shown")
    }

    private fun hideSystemUI() {
        activity.window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.hide(systemBars())
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (SYSTEM_UI_FLAG_IMMERSIVE
                        or SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }

        Log.d(TAG, "System UI hidden")
    }

    private fun updateEnabled() {
        enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
    }

    override fun onSettingChanged(key: String) {
        if (key == PREF_FULLSCREEN_ENABLED) updateEnabled()
    }

    override fun onModeChanged(animate: Boolean) {
        if (enabled) {
            fullscreen = (mode == MODE_INACTIVE)
        }
    }

    companion object {

        private const val TAG = "FullscreenControl"
    }
}