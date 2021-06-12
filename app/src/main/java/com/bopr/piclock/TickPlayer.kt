package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getResourceId

internal class TickPlayer(private val context: Context) {

    private val TAG = "TickPlayer"

    private lateinit var player: MediaPlayer
    private val volumeAnimator: ObjectAnimator = ObjectAnimator().apply {
        setPropertyName("volume")
        duration = 3000
        interpolator = LinearInterpolator()
    }
    private var ready: Boolean = false

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
            Log.d(TAG, "Playing")
            player.run {
                seekTo(0)
                start()
            }
        }
    }

    fun stop() {
        if (ready) {
            player.stop()
            player.release()
            ready = false
            Log.d(TAG, "Stopped")
        }
    }

    fun fadeIn(onEnd: () -> Unit = {}) {
        if (ready) {
            volumeAnimator.apply {
                cancel()

                target = player
                setFloatValues(0.0f, 1.0f)
                doOnEnd { onEnd() }

                start()
            }
        }
    }

    fun fadeOut(onEnd: () -> Unit = {}) {
        if (ready) {
            volumeAnimator.apply {
                cancel()

                target = player
                setFloatValues(1.0f, 0.0f)
                doOnEnd { onEnd() }

                start()
            }
        }
    }

}
