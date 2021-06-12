package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getResourceId

internal class TickPlayer(private val context: Context) {

    private lateinit var player: MediaPlayer
    private var volumeAnimator: ObjectAnimator? = null
    private var ready: Boolean = false
//    private val handler = Handler(Looper.getMainLooper())
//    private val timerTask = object : Runnable {
//
//        override fun run() {
//            onTimer()
//            handler.postDelayed(this, 1000)
//        }
//    }

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
            }
        }
    }

    fun stop() {
        if (ready) {
            player.stop()
            player.release()
            ready = false
        }
    }

    fun fadeOut(onEnd: () -> Unit = {}) {
        if (ready) {
            volumeAnimator?.cancel()
            volumeAnimator = ObjectAnimator().apply {
                target = player
                setPropertyName("volume")
                setFloatValues(1.0f, 0.0f)
                duration = 3000
                interpolator = LinearInterpolator()
                doOnEnd { onEnd() }

                start()
            }
        }
    }

    fun play() {
        if (!ready) {
            prepare()
        }

        if (ready) {
            player.run {
                seekTo(0)
                start()
            }
        }
    }

}
