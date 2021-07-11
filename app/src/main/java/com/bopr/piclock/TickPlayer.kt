package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.util.Property
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
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

    var changingVolume = false

    private fun prepare() {
        if (!prepared) {
            val resId = context.getResId("raw", soundName)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)
                setVolume(1f)
                prepared = true

                Log.d(_tag, "Prepared")
            }
        }
    }

    fun setSound(name: String) {
        stop()
        soundName = name
    }

    fun setVolume(value: Float) {
        if (prepared) {
            player.setVolume(value, value)

            Log.d(_tag, "Volume reset to: $value")
        }
    }

    fun fadeVolume(fadeDuration: Long, vararg volumes: Float) {
        prepare()
        Log.d(_tag, "Start fading : ${volumes.joinToString()}")

        changingVolume = true
        volumeAnimator.run {
            if (isRunning) end()
            removeAllListeners()

            target = player  /* player reference changes in prepare() ! */
            duration = fadeDuration
            setFloatValues(*volumes)
            doOnEnd {
                Log.d(_tag, "End fading")

                changingVolume = false
            }

            start()
        }
    }

    fun play() {
//            Log.v(_tag, "Tik")
        prepare()
        player.run {
            seekTo(0)
            start()
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
