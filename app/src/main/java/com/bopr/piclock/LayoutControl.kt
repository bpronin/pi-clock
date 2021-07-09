package com.bopr.piclock

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.MarginLayoutParams
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.fabMargin

/**
 * Convenience class to control content view layouts.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class LayoutControl(
    private val rootView: ConstraintLayout,
    private val fragmentManager: FragmentManager
) {

    private val _tag = "LayoutControl"

    private val screenOrientation get() = rootView.resources.configuration.orientation
    private val infoView get() = rootView.findViewById<View>(R.id.info_view)
    private val settingsButton get() = rootView.findViewById<View>(R.id.settings_button)
    private val settingsContainer get() = rootView.findViewById<View>(R.id.settings_container)
    private val fabMargin = rootView.resources.fabMargin
    private var wantRecreateActivity = false

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

    init {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            adjustMargins(windowInsets)
            windowInsets
        }
    }

    private fun createDefaultConstraints() = ConstraintSet().apply {
        R.id.settings_button.let {
            constrainWidth(it, WRAP_CONTENT)
            constrainHeight(it, WRAP_CONTENT)
        }
    }

    private fun createSettingsView() {
        fragmentManager.run {
            findFragmentByTag(SETTINGS_FRAGMENT_TAG) ?: run {
                beginTransaction()
                    .replace(R.id.settings_container, SettingsFragment(), SETTINGS_FRAGMENT_TAG)
                    .commit()

                Log.d(_tag, "Added settings fragment")
            }
        }
    }

    private fun removeSettingsView() {
        fragmentManager.run {
            findFragmentByTag(SETTINGS_FRAGMENT_TAG)?.run {
                beginTransaction()
                    .remove(this)
                    .commit()

                Log.d(_tag, "Removed settings fragment")

                if (wantRecreateActivity) {
                    wantRecreateActivity = false
                    activity?.recreate()
                }
            }
        }

    }

    private fun adjustMargins(windowInsets: WindowInsetsCompat) {
        Log.d(_tag, "Adjusting margins")

        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

        TransitionManager.beginDelayedTransition(rootView)

        infoView.updateLayoutParams<MarginLayoutParams> {
            leftMargin = fabMargin + insets.left
            topMargin = fabMargin + insets.top
        }
        settingsButton.updateLayoutParams<MarginLayoutParams> {
            rightMargin = fabMargin + insets.right
            bottomMargin = fabMargin + insets.bottom
        }
        settingsContainer.updateLayoutParams<MarginLayoutParams> {
            rightMargin = insets.right
            bottomMargin = insets.bottom
        }
    }

    fun onModeChanged(@Mode mode: Int, animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(rootView)
        }

        when (mode) {
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

    fun onFullscreenEnabled(enabled: Boolean) {
        wantRecreateActivity = !enabled
    }

    companion object {

        private const val SETTINGS_FRAGMENT_TAG = "settings_fragment"
    }
}

