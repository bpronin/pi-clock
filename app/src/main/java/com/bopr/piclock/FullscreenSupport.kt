package com.bopr.piclock

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

class FullscreenSupport(private val window: Window) {

    private val TAG = "FullscreenSupport"

    private val handler = Handler(Looper.getMainLooper())

    var onChange: (Boolean) -> Unit = {}
    var autoTurnOnDelay = 5000L

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private val startDelay = 300L

    private val turnOnTask = Runnable {
        showSystemUI()
        onChange(fullscreen)
    }

    private val turnOffTask = Runnable {
        hideSystemUI()
        onChange(fullscreen)
    }

    private val autoTurnOnTask = Runnable {
        Log.d(TAG, "Auto")
        fullscreen = true
    }

    var fullscreen: Boolean = false
        set(value) {
            handler.removeCallbacks(autoTurnOnTask)
            field = value
            if (field) {
                handler.postDelayed(turnOffTask, startDelay)
            } else {
                handler.postDelayed(turnOnTask, startDelay)
                handler.postDelayed(autoTurnOnTask, autoTurnOnDelay)
            }
            Log.d(TAG, "Fullscreen: $field")
        }

    fun toggle() {
        fullscreen = !fullscreen
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

        Log.d(TAG, "System UI shown")
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

        Log.d(TAG, "System UI hidden")
    }

}