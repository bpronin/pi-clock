package com.bopr.piclock

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.*
import android.view.WindowInsets.Type
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bopr.piclock.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var hoursView: TextView

    private var fullscreenMode: Boolean = false
        set(value) {
            /* NOTE: Some older devices needs a small delay between UI widget updates
            and a change of the status and navigation bar. */

            val handler = Handler(Looper.getMainLooper())
            field = value
            if (field) {
                handler.postDelayed(object : Runnable {

                    override fun run() {
                        handler.removeCallbacks(this)
                        hideSystemUI()
                    }
                }, 300)
            } else {
                handler.postDelayed(object : Runnable {

                    override fun run() {
                        handler.removeCallbacks(this)
                        showSystemUI()
                    }
                }, 300)
            }
        }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hoursView = binding.hoursView
        hoursView.setOnClickListener {
            fullscreenMode = !fullscreenMode
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls
         are available. */
        fullscreenMode = true
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.show(Type.statusBars() or Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(Type.statusBars() or Type.navigationBars())
                it.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_FULLSCREEN
                    or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }
}