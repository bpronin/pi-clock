package com.bopr.piclock

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.util.fabMargin

internal class LayoutControl(private val rootView: ConstraintLayout) {

    private val fabMargin = rootView.resources.fabMargin

    private val constraintsDefault = ConstraintSet().apply {
        R.id.settings_button.let {
            constrainWidth(it, WRAP_CONTENT)
            constrainHeight(it, WRAP_CONTENT)
            connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
            connect(it, BOTTOM, R.id.main_container, BOTTOM, fabMargin)
        }
    }

    private val constraintsEditor = mapOf(
        ORIENTATION_PORTRAIT to ConstraintSet().apply {
            R.id.settings_button.let {
                constrainWidth(it, WRAP_CONTENT)
                constrainHeight(it, WRAP_CONTENT)
                connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
                connect(it, BOTTOM, R.id.main_container, BOTTOM)
                connect(it, TOP, R.id.main_container, BOTTOM)
            }
        },
        ORIENTATION_LANDSCAPE to ConstraintSet().apply {
            R.id.settings_button.let {
                constrainWidth(it, WRAP_CONTENT)
                constrainHeight(it, WRAP_CONTENT)
                connect(it, BOTTOM, PARENT_ID, BOTTOM, fabMargin)
                connect(it, LEFT, R.id.main_container, RIGHT)
                connect(it, RIGHT, R.id.main_container, RIGHT)
            }
        })

    fun onModeChanged(mode: Int, animate: Boolean) {
        val orientation = rootView.resources.configuration.orientation
        val button = rootView.findViewById<View>(R.id.settings_button)
        val container = rootView.findViewById<View>(R.id.settings_container)

        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }

        when (mode) {
            MODE_ACTIVE -> {
                constraintsDefault.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = GONE
            }
            MODE_INACTIVE -> {
                constraintsDefault.applyTo(rootView)
                button.visibility = GONE
                container.visibility = GONE
            }
            MODE_EDITOR -> {
                constraintsEditor[orientation]!!.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = VISIBLE
            }
        }
    }
}

