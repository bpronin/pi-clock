package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getResId
import com.bopr.piclock.util.property.PROP_VOLUME

/**
 * Plays tick sounds.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class TickPlayer(private val context: Context) {

    private val _tag = "TickPlayer"
    private val volumeAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(player, PROP_VOLUME, 0f).apply {
            interpolator = LinearInterpolator()
        }
    }

    private lateinit var sourceName: String

    private var player: MediaPlayer? = null

    var changingVolume = false
        private set

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

            target = player
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
