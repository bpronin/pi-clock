package com.bopr.piclock

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log
import android.util.Property
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.Animations.Companion.FLOAT_CONTENT_DURATION
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND_MODE
import com.bopr.piclock.Settings.Companion.TICK_ACTIVE
import com.bopr.piclock.Settings.Companion.TICK_FLOATING
import com.bopr.piclock.Settings.Companion.TICK_INACTIVE
import com.bopr.piclock.util.getResId

/**
 * Controls ticking sound.
 */
internal class TickControl(private val context: Context, private val settings: Settings) :
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val _tag = "TickControl"

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
    private lateinit var modes: Set<String>
    private var changingVolume = false
    private var prepared = false

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
        updateModes()
    }

    private fun prepare() {
        val resId = context.getResId("raw", settings.getString(PREF_TICK_SOUND))
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

    private fun stop() {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(_tag, "Setting: $key changed to: ${settings.all[key]}")
        if (key == PREF_TICK_SOUND) {
            stop()
        } else if (key == PREF_TICK_SOUND_MODE) {
            updateModes()
        }
    }

    private fun updateModes() {
        modes = settings.getStringSet(PREF_TICK_SOUND_MODE)
    }

    fun destroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun onChangeMode(mode: Int) {
        when (mode) {
            MODE_ACTIVE -> if (!modes.containsAll(setOf(TICK_ACTIVE, TICK_INACTIVE))) {
                when {
                    modes.contains(TICK_ACTIVE) -> fadeVolume(4000, 0f, 1f)
                    modes.contains(TICK_INACTIVE) -> fadeVolume(4000, 1f, 0f)
                }
            }
            MODE_INACTIVE -> if (!modes.containsAll(setOf(TICK_ACTIVE, TICK_INACTIVE))) {
                when {
                    modes.contains(TICK_ACTIVE) -> fadeVolume(4000, 1f, 0f)
                    modes.contains(TICK_INACTIVE) -> fadeVolume(4000, 0f, 1f)
                }
            }
            MODE_EDITOR -> stop()
        }
    }

    fun onFloatContent(floating: Boolean) {
        if (floating && modes.contains(TICK_FLOATING)) {
            fadeVolume(FLOAT_CONTENT_DURATION, 0f, 1f, 0f)
        }
    }

    fun onTick(mode: Int, floating: Boolean) {
        if ((modes.contains(TICK_ACTIVE) && mode == MODE_ACTIVE)
            || (modes.contains(TICK_INACTIVE) && mode == MODE_INACTIVE)
            || (modes.contains(TICK_FLOATING) && floating)
            || changingVolume
        ) {
            play()
        }
    }

}
