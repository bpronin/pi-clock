package com.bopr.piclock

import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.bopr.piclock.util.mainHandler

class FullscreenSupport(private val window: Window) {

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

    var enabled = true
        set(value) {
            if (field != value) {
                if (!value) {
                    fullscreen = false
                }
                field = value
            }
        }

    var fullscreen: Boolean = false
        set(value) {
            if (enabled && field != value) {
                mainHandler.removeCallbacksAndMessages(null)
                field = value
                if (field) {
                    mainHandler.postDelayed(turnOffTask, startDelay)
                } else {
                    mainHandler.postDelayed(turnOnTask, startDelay)
                }
                Log.d(_tag, "Fullscreen: $field")
            }
        }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }

        Log.d(_tag, "System UI shown")
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }

        Log.d(_tag, "System UI hidden")
    }

}