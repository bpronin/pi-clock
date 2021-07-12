package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.AccelerateInterpolator
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
    private val volumeAnimator by lazy {
        ObjectAnimator.ofFloat(player, PROP_VOLUME, 0f).apply {
//            interpolator = LinearInterpolator()
            interpolator = AccelerateInterpolator()
        }
    }

    private lateinit var sourceName: String

    private var player: MediaPlayer? = null

    var fadingVolume = false
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

        player?.run {
            fadingVolume = true
            volumeAnimator.run {
                if (isRunning) end()
                removeAllListeners()

                target = player
                duration = fadeDuration
                setFloatValues(*volumes)
                doOnEnd {
                    Log.v(_tag, "End fading volume")

                    fadingVolume = false
                }

                start()
            }
        }
    }

    fun play() {
        prepare()
        player?.run {
            seekTo(0, )
            start()

//        Log.v(_tag, "Tick!")
        }
    }

    fun stop() {
        volumeAnimator.cancel()
        player = player?.run {
            stop()
            reset()
            release()
            null
        }

        Log.d(_tag, "Stopped")
    }

}
