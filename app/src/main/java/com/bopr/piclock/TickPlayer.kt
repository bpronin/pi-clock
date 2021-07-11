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

/**
 * Convenience class play tick sound.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class TickPlayer(private val context: Context) {

    private val _tag = "TickPlayer"

    private val volumeAnimator: ObjectAnimator by lazy {
        val volumeProperty = object : Property<MediaPlayer, Float>(Float::class.java, "volume") {

            override fun get(player: MediaPlayer): Float {
                throw UnsupportedOperationException()
            }

            override fun set(player: MediaPlayer, value: Float) {
                player.setVolume(value, value)
            }
        }

        ObjectAnimator.ofFloat(player, volumeProperty, 0f).apply {
            interpolator = LinearInterpolator()
        }
    }

    private lateinit var player: MediaPlayer
    private lateinit var soundName: String
    private var prepared = false

    private fun prepare() {
        val resId = context.getResId("raw", soundName)
        if (resId != 0) {
            player = MediaPlayer.create(context, resId)
            resetVolume()
            prepared = true

            Log.d(_tag, "Prepared")
        }
    }

    fun setSound(name: String) {
        stop()
        soundName = name
    }

    fun resetVolume() {
        if (prepared) {
            player.setVolume(1f, 1f)

            Log.d(_tag, "Volume reset to max")
        }
    }

    fun fadeVolume(fadeDuration: Long, volumeFrom: Float, volumeTo: Float, onEnd: () -> Unit = {}) {
        if (prepared) {
            volumeAnimator.run {
                if (isRunning) end()
                removeAllListeners()

                target = player  /* player changes instance in prepare() ! */
                duration = fadeDuration
                setFloatValues(volumeFrom, volumeTo)
                doOnStart {
                    Log.d(_tag, "Start fading from: $volumeFrom to: $volumeTo")
                }
                doOnEnd {
                    Log.d(_tag, "End fading")

                    onEnd()
                }

                start()
            }
        }
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
            prepared = false
            volumeAnimator.cancel()
            player.stop()
            player.reset()
            player.release()

            Log.d(_tag, "Stopped")
        }
    }

}
