package com.bopr.piclock

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View.*
import android.view.WindowInsets.Type.systemBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED

/**
 * Convenience class to control system UI's visibility.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class FullscreenControl(
    private val activity: Activity,
    private val handler: Handler,
    private val settings: Settings
) {

    private val _tag = "FullscreenSupport"

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

                Log.d(_tag, "Fullscreen: $field")
            }
        }

    private var enabled = true

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.apply {
                setDecorFitsSystemWindows(false)
                insetsController?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
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

        Log.d(_tag, "System UI shown")
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

        Log.d(_tag, "System UI hidden")
    }

    fun init() {
        enabled = settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        if (!enabled) {
            fullscreen = false
        }
    }

    fun onSettingChanged(key: String) {
        if (key == PREF_FULLSCREEN_ENABLED) init()
    }

    fun onModeChanged(@Mode mode: Int) {
        if (enabled) {
            fullscreen = (mode == MODE_INACTIVE)
        }
    }

}