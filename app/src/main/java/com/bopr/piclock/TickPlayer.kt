package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.util.Property
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.util.getResId

internal class TickPlayer(private val context: Context) {

    var changingVolume: Boolean = false
        private set

    /** Logger tag. */
    private val _tag = "TickPlayer"

    private lateinit var player: MediaPlayer

    private val volumeAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(player, VolumeProperty(), 0f).apply {
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
                resetVolume()
                prepared = true

                Log.d(_tag, "Prepared")
            }
        }
    }

    private fun resetVolume() {
        player.setVolume(1f, 1f)

        Log.d(_tag, "Volume reset to max")
    }

    fun play() {
        if (!prepared) {
            prepare()
        }

        if (prepared) {
//            Log.v(_tag, "Tik")

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

    fun fadeVolume(changeDuration: Long, vararg volumeValues: Float) {
        if (prepared) {
            volumeAnimator.run {
                if (isRunning) end()
                removeAllListeners()

                target = player  /* player changes instance in prepare() ! */
                duration = changeDuration
                setFloatValues(*volumeValues)
                doOnStart {
                    Log.d(_tag, "Start changing volume $volumeValues")

                    changingVolume = true
                }
                doOnEnd {
                    Log.d(_tag, "End changing volume")

                    changingVolume = false
                }

                start()
            }
        }
    }

    private class VolumeProperty : Property<MediaPlayer, Float>(Float::class.java, "volume") {

        private var volume = 1f

        override fun get(player: MediaPlayer): Float {
            return volume
        }

        override fun set(player: MediaPlayer, value: Float) {
            volume = value
            player.setVolume(volume, volume)
        }
    }
}
