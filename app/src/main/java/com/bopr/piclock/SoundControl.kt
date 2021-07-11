package com.bopr.piclock

import android.content.Context
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_TICK_RULES
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.Settings.Companion.TICK_ACTIVE
import com.bopr.piclock.Settings.Companion.TICK_FLOATING
import com.bopr.piclock.Settings.Companion.TICK_INACTIVE

/**
 * Convenience class to control app sounds.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class SoundControl(
    context: Context,
    private val settings: Settings
) {

    private val _tag = "SoundControl"

    private val player = TickPlayer(context)
    private var whenActive = false
    private var whenInactive = false
    private var whenFloating = false

    @Mode
    private var mode = MODE_INACTIVE
    private var viewFloating = false
    private val fadeDuration = 4000L //todo: make it var

    init {
        updateSoundName()
        updatePlayRules()
    }

    fun onModeChanged(@Mode value: Int, animate: Boolean) {
        mode = value
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

    fun onFloatView(floating: Boolean) {
        viewFloating = floating
        if (viewFloating && whenFloating && !whenInactive) {
            player.fadeVolume(fadeDuration * 2, 0f, 1f, 0f)
        }
    }

    fun onTimer(tick: Int) {
        if (tick % 2 != 0) return

        if ((whenActive && mode == MODE_ACTIVE)
            || (whenInactive && mode == MODE_INACTIVE)
            || (whenFloating && viewFloating)
            || player.changingVolume
        ) player.play()
    }

    private fun updateSoundName() {
        player.setSound(settings.getString(PREF_TICK_SOUND))
    }

    private fun updatePlayRules() {
        val rules = settings.getStringSet(PREF_TICK_RULES)
        whenFloating = rules.contains(TICK_FLOATING)
        whenInactive = rules.contains(TICK_INACTIVE)
        whenActive = rules.contains(TICK_ACTIVE)
    }

    fun destroy() {
        player.stop()
    }

    fun onSettingChanged(key: String) {
        when (key) {
            PREF_TICK_SOUND -> updateSoundName()
            PREF_TICK_RULES -> updatePlayRules()
        }
    }

}
