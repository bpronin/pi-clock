package com.bopr.piclock

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View.*
import android.view.Window

class FullscreenSupport(private val window: Window) {

    /** Logger tag. */
    private val _tag = "FullscreenSupport"

    private val handler = Handler(Looper.getMainLooper())

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
                handler.removeCallbacksAndMessages(null)
                field = value
                if (field) {
                    handler.postDelayed(turnOffTask, startDelay)
                } else {
                    handler.postDelayed(turnOnTask, startDelay)
                }

                Log.d(_tag, "Fullscreen: $field")
            }
        }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun showSystemUI() {
        /* new API produces flickering */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setDecorFitsSystemWindows(true)
//            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//        } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//        }

        Log.d(_tag, "System UI shown")
    }

    //todo: after rotating it does not hide UI
    private fun hideSystemUI() {
        /* new API produces flickering */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setDecorFitsSystemWindows(false)
//            window.insetsController?.let {
//                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//                it.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_IMMERSIVE
                or SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN)
//        }

        Log.d(_tag, "System UI hidden")
    }

}