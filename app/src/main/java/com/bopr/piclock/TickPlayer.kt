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

    private var player: MediaPlayer? = null
    private lateinit var sourceName: String

    var changingVolume = false

    private fun prepare() {
        if (player == null) {
            val resId = context.getResId("raw", sourceName)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)

                Log.d(_tag, "Prepared")
            } else {
                player = null

                Log.d(_tag, "Zero resource")
            }
        }
    }

    fun setSource(name: String) {
        stop()
        sourceName = name
    }

    fun fadeVolume(fadeDuration: Long, vararg volumes: Float) {
        Log.d(_tag, "Start fading volume: ${volumes.joinToString()} during: $fadeDuration")

        prepare()
        changingVolume = true
        volumeAnimator.run {
            if (isRunning) end()
            removeAllListeners()

            target = player  /* player reference changes in prepare() ! */
            duration = fadeDuration
            setFloatValues(*volumes)
            doOnEnd {
                Log.v(_tag, "End fading volume")

                changingVolume = false
            }

            start()
        }
    }

    fun play() {
//        Log.v(_tag, "Tick!")

        prepare()
        player?.run {
            seekTo(0)
            start()
        }
    }

    fun stop() {
        volumeAnimator.cancel()
        player?.apply {
            stop()
            reset()
            release()
            player = null
        }

        Log.d(_tag, "Stopped")
    }

}
