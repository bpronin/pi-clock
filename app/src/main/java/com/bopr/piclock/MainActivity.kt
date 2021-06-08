package com.bopr.piclock

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bopr.piclock.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private lateinit var hoursView: TextView
    private lateinit var binding: ActivityMainBinding
    private lateinit var fullscreenSupport:FullscreenSupport
    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val hoursFormat = SimpleDateFormat("HH:MM", Locale.getDefault())
    private val secondsFormat = SimpleDateFormat("ss", Locale.getDefault())

    private val timerTask = object : Runnable {

        override fun run() {
            updateTimeControls()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullscreenSupport = FullscreenSupport(window)

        hoursView = binding.hoursView
        hoursView.setOnClickListener {
            fullscreenSupport.fullscreen = !fullscreenSupport.fullscreen
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Trigger the fullscreen mode shortly after the activity has been
         created, to briefly hint to the user that UI controls
         are available. */
        fullscreenSupport.fullscreen = true
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerTask)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        TODO("Not yet implemented")
    }

    private fun updateTimeControls() {
        val time = Date()
        hoursView.text = hoursFormat.format(time)
    }


}