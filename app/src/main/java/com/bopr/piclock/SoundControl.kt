package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.util.Property
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.TICK_ACTIVE
import com.bopr.piclock.Settings.Companion.TICK_FLOATING
import com.bopr.piclock.Settings.Companion.TICK_INACTIVE
import com.bopr.piclock.util.getResId

/**
 * Convenience class to control app sounds.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SoundControl(private val context: Context) {

    private val _tag = "SoundControl"

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
    private var whenActive: Boolean = false
    private var whenInactive: Boolean = false
    private var whenFloating: Boolean = false
    private var changingVolume = false
    private var prepared = false

    @Mode
    private var mode = MODE_INACTIVE
    private var viewFloating = false

    private fun prepare() {
        val resId = context.getResId("raw", soundName)
        if (resId != 0) {
            player = MediaPlayer.create(context, resId)
            resetVolume()
            prepared = true

            Log.d(_tag, "Prepared")
        }
    }

    private fun play() {
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

    private fun resetVolume() {
        player.setVolume(1f, 1f)

        Log.d(_tag, "Volume reset to max")
    }

    private fun fadeVolume(changeDuration: Long, vararg volumeValues: Float) {
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

    fun setSound(name: String) {
        stop()
        this.soundName = name
    }

    fun setRules(rules: Set<String>) {
        whenFloating = rules.contains(TICK_FLOATING)
        whenInactive = rules.contains(TICK_INACTIVE)
        whenActive = rules.contains(TICK_ACTIVE)
    }

    fun onModeChanged(@Mode mode: Int, animate: Boolean) {
        this.mode = mode
        if (animate) {
            when (mode) {
                MODE_ACTIVE -> if (!(whenActive && whenInactive)) {
                    when {
                        whenActive -> fadeVolume(4000, 0f, 1f)
                        whenInactive -> fadeVolume(4000, 1f, 0f)
                    }
                }
                MODE_INACTIVE -> if (!(whenActive && whenInactive)) {
                    when {
                        whenActive -> fadeVolume(4000, 1f, 0f)
                        whenInactive -> fadeVolume(4000, 0f, 1f)
                    }
                }
                MODE_EDITOR -> stop()
            }
        }
    }

    fun onFloatView(floating: Boolean) {
        this.viewFloating = floating
        if (viewFloating && whenFloating) {
            fadeVolume(10000L, 0f, 1f, 0f)
        }
    }

    fun onTimer(halfTick: Int) {
        if (halfTick % 2 != 0) return

        if ((whenActive && mode == MODE_ACTIVE)
            || (whenInactive && (mode == MODE_INACTIVE || mode == MODE_EDITOR))
            || (whenFloating && viewFloating)
            || changingVolume
        ) play()
    }

}
