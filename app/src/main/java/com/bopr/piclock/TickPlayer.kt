package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import com.bopr.piclock.util.getResourceId

internal class TickPlayer(private val context: Context) {

    private val TAG = "TickPlayer"

    private lateinit var player: MediaPlayer
    private val volumeAnimator = ObjectAnimator().apply {
        setPropertyName("volume")
        interpolator = LinearInterpolator()
    }
    private var ready = false

    var soundName: String? = null
        set(value) {
            if (field != value) {
                stop()
                field = value
            }
        }

    private fun prepare() {
        soundName?.let {
            val resId = context.getResourceId("raw", it)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)
                ready = true

                Log.d(TAG, "Ready")
            }
        }
    }

    fun play() {
        if (!ready) {
            prepare()
        }

        if (ready) {
//            Log.d(TAG, "Playing")
            player.run {
                seekTo(0)
                start()
            }
        }
    }

    fun stop() {
        if (ready) {
            volumeAnimator.cancel()
            player.stop()
            player.release()
            ready = false

            Log.d(TAG, "Stopped")
        }
    }

    fun fadeVolume(from: Float, to: Float, duration: Long) {
        if (ready) {
            player.setVolume(from, from)

            volumeAnimator.also {
                it.cancel()

                it.target = player
                it.duration = duration
                it.setFloatValues(from, to)

                it.start()
            }
        }
    }

}
