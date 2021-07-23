package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.Contextual
import com.bopr.piclock.util.getRawResId
import com.bopr.piclock.util.property.PROP_VOLUME

/**
 * Plays tick sounds.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class TickPlayer(private val context: Context) : Contextual {

    private val volumeAnimator by lazy {
        ObjectAnimator.ofFloat(player, PROP_VOLUME, 0f).apply {
            interpolator = LinearInterpolator()
        }
    }

    private lateinit var sourceName: String

    private var player: MediaPlayer? = null

    var fadingVolume = false
        private set

    override fun requireContext(): Context {
        return context
    }

    private fun prepare() {
        if (player == null) {
            val resId = getRawResId(sourceName)
            if (resId != 0) {
                player = MediaPlayer.create(context, resId)

                Log.d(TAG, "Prepared")
            } else {
                player = null

                Log.d(TAG, "No sound selected")
            }
        }
    }

    fun setSource(name: String) {
        stop()
        sourceName = name
    }

    fun fadeVolume(fadeDuration: Long, vararg volumes: Float) {
        Log.d(TAG, "Start fading volume: ${volumes.joinToString()} during: $fadeDuration")

        player?.run {
            fadingVolume = true
            volumeAnimator.run {
                if (isRunning) end()
                removeAllListeners()

                target = player
                duration = fadeDuration
                setFloatValues(*volumes)
                doOnEnd {
                    Log.v(TAG, "End fading volume")

                    fadingVolume = false
                }

                start()
            }
        }
    }

    fun play() {
        prepare()
        player?.run {
            seekTo(0)
            start()

//        Log.v(TAG, "Tick!")
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

        Log.d(TAG, "Stopped")
    }

    companion object {

        private const val TAG = "TickPlayer"
    }
}
