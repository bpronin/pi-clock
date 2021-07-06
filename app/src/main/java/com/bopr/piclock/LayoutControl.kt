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
import com.bopr.piclock.util.fabMargin

internal class LayoutControl(
    private val rootView: ConstraintLayout,
    private val fragmentManager: FragmentManager
) {

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
            }
        }
    }

    fun onModeChanged(@Mode mode: Int, animate: Boolean) {
        val orientation = rootView.resources.configuration.orientation
        val button = rootView.findViewById<View>(R.id.settings_button)
        val container = rootView.findViewById<View>(R.id.settings_container)

        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }

        when (mode) {
            MODE_ACTIVE -> {
                mainConstraints.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = GONE
                removeSettingsView()
            }
            MODE_INACTIVE -> {
                mainConstraints.applyTo(rootView)
                button.visibility = GONE
                container.visibility = GONE
                removeSettingsView()
            }
            MODE_EDITOR -> {
                createSettingsView()
                editorConstraints[orientation]?.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = VISIBLE
            }
        }
    }

    companion object {

        private const val SETTINGS_FRAGMENT_TAG = "settings_fragment"
    }
}

