package com.bopr.piclock

import android.content.Context
import android.media.MediaPlayer
import com.bopr.piclock.util.getResourceId

internal class TickPlayer(private val context: Context) {

    private lateinit var player: MediaPlayer
    private var ready: Boolean = false

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
            }
            ready = true
        }
    }

    fun stop() {
        if (ready) {
            player.stop()
            player.release()
            ready = false
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
