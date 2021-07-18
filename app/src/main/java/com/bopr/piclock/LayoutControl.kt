package com.bopr.piclock

import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet.UNSET
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.util.*

/**
 * Convenience class to control content view layouts.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class LayoutControl(
    private val rootView: ConstraintLayout,
    private val fragmentManager: FragmentManager,
    settings: Settings
) : ContentControlAdapter(settings) {

    private val settingsButton by lazy { rootView.findViewById<View>(R.id.settings_button) }
    private val settingsContainer by lazy { rootView.findViewById<View>(R.id.settings_container) }
    private val defaultSettingsButtonInsets by lazy { settingsButton.marginsToInsets() }
    private val defaultSettingsContainerInsets by lazy { settingsContainer.marginsToInsets() }

    private var requreRecreateActivity = false

    init {
        setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            settingsButton.fitIntoWindow(insets, defaultSettingsButtonInsets)
            settingsContainer.fitIntoWindow(insets, defaultSettingsContainerInsets)
            windowInsets
        }
    }

    private fun updateSettingsButtonLayout(editorMode: Boolean) {
        settingsButton.updateLayoutParams<LayoutParams> {
            if (editorMode) {
                if (rootView.isPortraitScreen)
                    topToBottom = R.id.content_container
                else
                    startToEnd = R.id.content_container
            } else {
                topToBottom = UNSET
                startToEnd = UNSET
            }
        }
    }

    private fun updateSettingsViewLayout(editorMode: Boolean) {
        fragmentManager.apply {
            if (editorMode) {
                replaceFragment(R.id.settings_container, ::SettingsFragment)

                Log.d(TAG, "Added settings fragment")
            } else {
                removeFragment(R.id.settings_container) {
                    if (requreRecreateActivity) {
                        Log.d(TAG, "Activity recreation required")

                        requreRecreateActivity = false
                        it.activity?.recreate()
                    }
                }
                Log.d(TAG, "Removed settings fragment")
            }
        }
    }

    override fun onSettingChanged(key: String) {
        if (key == PREF_FULLSCREEN_ENABLED) {
            /* when the setting is disabled we need to recreate activity to reset entire layout */
            requreRecreateActivity = !settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }
    }

    override fun onModeChanged(animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }

        when (mode) {
            MODE_ACTIVE -> {
                updateSettingsButtonLayout(false)
                updateSettingsViewLayout(false)
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = GONE
            }
            MODE_INACTIVE -> {
                updateSettingsButtonLayout(false)
                updateSettingsViewLayout(false)
                settingsButton.visibility = GONE
                settingsContainer.visibility = GONE
            }
            MODE_EDITOR -> {
                updateSettingsButtonLayout(true)
                updateSettingsViewLayout(true)
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = VISIBLE
            }
        }
    }

    companion object {

        private const val TAG = "LayoutControl"
    }

}

