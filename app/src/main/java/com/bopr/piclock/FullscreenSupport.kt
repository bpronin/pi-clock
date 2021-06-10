package com.bopr.piclock

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

class FullscreenSupport(private val window: Window) {

    var onChange: (Boolean) -> Unit = {}
    var autoFullscreenDelay = 3000L

    private val handler = Handler(Looper.getMainLooper())
    private val fullscreenDelay = 300L
    private var autoFullscreenTask: Runnable? = null

    var fullscreen: Boolean = false
        set(value) {
            removeTask(autoFullscreenTask)
            field = value
            if (field) {
                runTask(fullscreenDelay) {
                    hideSystemUI()
                }
            } else {
                runTask(fullscreenDelay) {
                    showSystemUI()
                    autoFullscreenTask = runTask(autoFullscreenDelay) {
                        autoFullscreenTask = null
                        fullscreen = true
                    }
                }
            }
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
    }

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private fun runTask(delay: Long, action: () -> Unit): Runnable {
        val task = object : Runnable {

            override fun run() {
                removeTask(this)
                action()
                onChange(fullscreen)
            }
        }
        handler.postDelayed(task, delay)
        return task
    }

    private fun removeTask(task: Runnable?) {
        task?.run { handler.removeCallbacks(this) }
    }

}