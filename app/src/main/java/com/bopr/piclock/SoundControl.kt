package com.bopr.piclock

import android.content.Context
import android.util.Log
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.TICK_ACTIVE
import com.bopr.piclock.Settings.Companion.TICK_FLOATING
import com.bopr.piclock.Settings.Companion.TICK_INACTIVE
import java.util.*

/**
 * Convenience class to control app sounds.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SoundControl(context: Context, settings: Settings) : ContentControl(settings) {

    private val player = TickPlayer(context)
    private val fadeDuration = 4000L //todo: make it var

    private var whenActive = false
    private var whenInactive = false
    private var whenFloating = false
    private var viewFloating = false

    init {
        updateSource()
        updatePlayRules()
    }

    private fun updateSource() {
        val resName = settings.getString(PREF_TICK_SOUND)
        player.setSource(resName)

        Log.v(TAG, "Source set to: $resName")
    }

    private fun updatePlayRules() {
        settings.getStringSet(PREF_TICK_RULES).apply {
            whenFloating = contains(TICK_FLOATING)
            whenInactive = contains(TICK_INACTIVE)
            whenActive = contains(TICK_ACTIVE)
        }

        Log.v(TAG, "Rules changed")
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_TICK_SOUND -> updateSource()
            PREF_TICK_RULES -> updatePlayRules()
        }
    }

    override fun onModeChanged(@Mode newMode: Int, animate: Boolean) {
        super.onModeChanged(newMode, animate)
        if (animate) {
            when (mode) {
                MODE_EDITOR ->
                    player.stop()
                MODE_ACTIVE ->
                    when {
                        whenActive && whenInactive -> {
                            /* do not fade */
                        }
                        whenActive ->
                            player.fadeVolume(fadeDuration, 0f, 1f)
                        whenInactive ->
                            player.fadeVolume(fadeDuration, 1f, 0f)
                    }
                MODE_INACTIVE -> {
                    when {
                        whenInactive && whenActive -> {
                            /* do not fade */
                        }
                        whenActive ->
                            player.fadeVolume(fadeDuration, 1f, 0f)
                        whenInactive ->
                            player.fadeVolume(fadeDuration, 0f, 1f)
                    }
                }
            }
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        if (tick % 2 != 0) return

        if ((whenActive && mode == MODE_ACTIVE)
            || (whenInactive && mode == MODE_INACTIVE)
            || (whenFloating && viewFloating)
            || player.fadingVolume
        ) player.play()
    }

    fun onFloatView(floating: Boolean) {
        viewFloating = floating
        if (viewFloating && whenFloating && !whenInactive) {
            player.fadeVolume(fadeDuration * 2, 0f, 1f, 0f)
        }
    }

    fun destroy() {
        player.stop()
    }

    companion object {

        private const val TAG = "SoundControl"
    }
}
