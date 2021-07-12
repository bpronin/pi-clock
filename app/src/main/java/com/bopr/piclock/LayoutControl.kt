package com.bopr.piclock

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.Settings.Companion.PREF_FULLSCREEN_ENABLED
import com.bopr.piclock.util.fabMargin

/**
 * Convenience class to control content view layouts.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class LayoutControl(
    private val rootView: ConstraintLayout,
    private val fragmentManager: FragmentManager,
    settings: Settings
) : ContentControl(settings) {

    private val _tag = "LayoutControl"
    private val fabMargin = rootView.resources.fabMargin
    private val mainConstraints = createDefaultConstraints().apply {
        R.id.settings_button.let {
            connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
            connect(it, BOTTOM, R.id.content_container, BOTTOM, fabMargin)
        }
    }
    private val editorConstraints = arrayMapOf(
        ORIENTATION_PORTRAIT to createDefaultConstraints().apply {
            R.id.settings_button.let {
                connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
                connect(it, BOTTOM, R.id.content_container, BOTTOM)
                connect(it, TOP, R.id.content_container, BOTTOM)
            }
        },
        ORIENTATION_LANDSCAPE to createDefaultConstraints().apply {
            R.id.settings_button.let {
                connect(it, BOTTOM, PARENT_ID, BOTTOM, fabMargin)
                connect(it, LEFT, R.id.content_container, RIGHT)
                connect(it, RIGHT, R.id.content_container, RIGHT)
            }
        })
    private val screenOrientation get() = rootView.resources.configuration.orientation
    private val settingsButton get() = rootView.findViewById<View>(R.id.settings_button)
    private val settingsContainer get() = rootView.findViewById<View>(R.id.settings_container)

    private var wantRecreateActivity = false

    private fun createDefaultConstraints() = ConstraintSet().apply {
        R.id.settings_button.let {
            constrainWidth(it, WRAP_CONTENT)
            constrainHeight(it, WRAP_CONTENT)
        }
    }

    private fun createSettingsView() {
        fragmentManager.run {
            findFragmentById(R.id.settings_container) ?: run {
                beginTransaction()
                    .replace(R.id.settings_container, SettingsFragment())
                    .commit()

                Log.d(_tag, "Added settings fragment")
            }
        }
    }

    private fun removeSettingsView() {
        fragmentManager.run {
            findFragmentById(R.id.settings_container)?.run {
                beginTransaction()
                    .remove(this)
                    .commit()

                Log.d(_tag, "Removed settings fragment")

                if (wantRecreateActivity) {
                    Log.d(_tag, "Activity recreation required")

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
                mainConstraints.applyTo(rootView)
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = GONE
                removeSettingsView()
            }
            MODE_INACTIVE -> {
                mainConstraints.applyTo(rootView)
                settingsButton.visibility = GONE
                settingsContainer.visibility = GONE
                removeSettingsView()
            }
            MODE_EDITOR -> {
                createSettingsView()
                editorConstraints[screenOrientation]?.applyTo(rootView)
                settingsButton.visibility = VISIBLE
                settingsContainer.visibility = VISIBLE
            }
        }
    }

}

