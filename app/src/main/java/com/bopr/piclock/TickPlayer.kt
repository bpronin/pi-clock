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

    private var prepared = false

    var soundName: String? = null
        set(value) {
            if (field != value) {
                stop()
                field = value

                Log.d(_tag, "Sound set to: $field")
            }
        }

    private fun prepare() {
        soundName?.apply {
            val resId = context.getResId("raw", this)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)
                prepared = true

                Log.d(_tag, "Prepared")
            }
        }
    }

    fun play() {
        if (!prepared) {
            prepare()
        }

        if (prepared) {
            Log.v(_tag, "Tik")

            player.run {
                seekTo(0)
                start()
            }
        }
    }

    fun stop() {
        if (prepared) {
            volumeAnimator.cancel()
            player.stop()
            player.release()
            prepared = false

            Log.d(_tag, "Stopped")
        }
    }

    fun fadeInVolume(onEnd: () -> Unit) {
        fadeVolume(0f, 1f, 3000, onEnd)
    }

    fun fadeOutVolume(onEnd: () -> Unit) {
        fadeVolume(1f, 0f, 5000, onEnd)
    }

    private fun fadeVolume(from: Float, to: Float, fadeDuration: Long, onEnd: () -> Unit) {
        if (prepared) {
            volumeAnimator.run {
                cancel()
                removeAllListeners()

                target = player  /* player changes instance in prepare() ! */
                duration = fadeDuration
                setFloatValues(from, to)
                doOnEnd { onEnd() }
                doOnCancel { onEnd() }

                start()
            }
        } else {
            onEnd()
        }
    }

}
