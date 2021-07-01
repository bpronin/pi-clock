package com.bopr.piclock

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View.*
import android.view.WindowInsets.Type.systemBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE

/**
 * Controls showing and hiding system UI.
 */
internal class FullscreenControl(private val activity: Activity, private val handler: Handler) {

    /** Logger tag. */
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
            if (enabled && field != value) {
                handler.removeCallbacks(turnOnTask)
                handler.removeCallbacks(turnOffTask)
                field = value
                if (field) {
                    handler.postDelayed(turnOffTask, startDelay)
                } else {
                    handler.postDelayed(turnOnTask, startDelay)
                }

                Log.d(_tag, "Fullscreen: $field")
            }
        }

    var enabled = true
        set(value) {
            if (field != value) {
                if (!value) {
                    fullscreen = false
                }
                field = value
            }
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.apply {
                setDecorFitsSystemWindows(false)
                insetsController?.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    fun onChangeViewMode(mode: Int) {
        fullscreen = (mode == MODE_INACTIVE)
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

    //todo: after rotating it does not hide UI
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

}