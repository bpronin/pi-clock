package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import com.bopr.piclock.util.getResId

internal class TickPlayer(private val context: Context) {

    /** Logger tag. */
    private val _tag = "TickPlayer"

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
            val resId = context.getResId("raw", it)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)
                ready = true

                Log.d(_tag, "Ready")
            }
        }
    }

    fun play() {
        if (!ready) {
            prepare()
        }

        if (ready) {
            player.run {
                Log.d(_tag, "tik")
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

            Log.d(_tag, "Stopped")
        }
    }

    fun fadeInVolume(duration: Long) {
        fadeVolume(0f, 1f, duration)
    }

    fun fadeOutVolume(duration: Long) {
        fadeVolume(1f, 0f, duration)
    }

    private fun fadeVolume(from: Float, to: Float, fadeDuration: Long) {
        if (ready) {
            volumeAnimator.apply {
                cancel()

                player.setVolume(from, from)

                target = player
                duration = fadeDuration
                setFloatValues(from, to)
                start()
            }
        }
    }

}
