package com.bopr.piclock

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getResId

@SuppressLint("ObjectAnimatorBinding")
internal class TickPlayer(private val context: Context) {

    /** Logger tag. */
    private val _tag = "TickPlayer"

    private lateinit var player: MediaPlayer

    private val volumeAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(player, "volume", 0f).apply {
            interpolator = LinearInterpolator()
        }
    }

    private var ready = false

    var soundName: String? = null
        set(value) {
            if (field != value) {
                stop()
                field = value

                Log.d(_tag, "Sound set to: $field")
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
            Log.d(_tag, "tik")

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

            Log.d(_tag, "Stopped")
        }
    }

    fun fadeInVolume(duration: Long, onEnd: () -> Unit) {
        fadeVolume(0f, 1f, duration, onEnd)
    }

    fun fadeOutVolume(duration: Long, onEnd: () -> Unit) {
        fadeVolume(1f, 0f, duration, onEnd)
    }

    private fun fadeVolume(from: Float, to: Float, fadeDuration: Long, onEnd: () -> Unit) {
        if (ready) {
            volumeAnimator.run {
                cancel()
                removeAllListeners()

                target = player
                duration = fadeDuration
                setFloatValues(from, to)
                doOnEnd { onEnd() }
                doOnCancel {
                    onEnd()
                }

                start()
            }
        }
    }

}
