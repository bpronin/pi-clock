package com.bopr.piclock

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet.UNSET
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED

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

    private val screenOrientation get() = rootView.resources.configuration.orientation
    private val settingsButton get() = rootView.findViewById<View>(R.id.settings_button)
    private val settingsContainer get() = rootView.findViewById<View>(R.id.settings_container)

    private var wantRecreateActivity = false

    private fun createSettingsView() {
        fragmentManager.run {
            findFragmentById(R.id.settings_container) ?: run {
                beginTransaction()
                    .replace(R.id.settings_container, SettingsFragment())
                    .commit()

                Log.d(TAG, "Added settings fragment")
            }
        }
    }

    private fun removeSettingsView() {
        fragmentManager.run {
            findFragmentById(R.id.settings_container)?.run {
                beginTransaction()
                    .remove(this)
                    .commit()

                Log.d(TAG, "Removed settings fragment")

                if (wantRecreateActivity) {
                    Log.d(TAG, "Activity recreation required")

                    wantRecreateActivity = false
                    activity?.recreate()
                }
            }
        }
    }

    override fun onSettingChanged(key: String) {
        if (key == PREF_FULLSCREEN_ENABLED) {
            wantRecreateActivity = !settings.getBoolean(PREF_FULLSCREEN_ENABLED)
        }
    }

    override fun onModeChanged(@Mode newMode: Int, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }
        when (newMode) {
            MODE_ACTIVE -> {
                settingsButton.updateLayoutParams<LayoutParams> {
                    topToBottom = UNSET
                    startToEnd = UNSET
                }
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = GONE
                removeSettingsView()
            }
            MODE_INACTIVE -> {
                settingsButton.updateLayoutParams<LayoutParams> {
                    topToBottom = UNSET
                    startToEnd = UNSET
                }
                settingsButton.visibility = GONE
                settingsContainer.visibility = GONE
                removeSettingsView()
            }
            MODE_EDITOR -> {
                settingsButton.updateLayoutParams<LayoutParams> {
                    if (screenOrientation == ORIENTATION_PORTRAIT) {
                        topToBottom = R.id.content_container
                    } else {
                        startToEnd = R.id.content_container
                    }
                }
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = VISIBLE
                createSettingsView()
            }
        }
    }

    companion object {

        private const val TAG = "LayoutControl"
    }

}

