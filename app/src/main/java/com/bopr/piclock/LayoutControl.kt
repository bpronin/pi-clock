package com.bopr.piclock

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

    private val fabMargin = rootView.context.fabMargin

    private val fabConstraintsDefault = ConstraintSet().apply {
        R.id.settings_button.let {
            constrainWidth(it, WRAP_CONTENT)
            constrainHeight(it, WRAP_CONTENT)
            connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
            connect(it, BOTTOM, R.id.main_container, BOTTOM, fabMargin)
        }
    }

    private val fabConstraintsEditor = ConstraintSet().apply {
        R.id.settings_button.let {
            constrainWidth(it, WRAP_CONTENT)
            constrainHeight(it, WRAP_CONTENT)
            connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
            connect(it, BOTTOM, R.id.main_container, BOTTOM)
            connect(it, TOP, R.id.main_container, BOTTOM)
        }
    }

    fun onModeChanged(mode: Int, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }

        val button = rootView.findViewById<View>(R.id.settings_button)
        val container = rootView.findViewById<View>(R.id.settings_container)
        when (mode) {
            MODE_ACTIVE -> {
                fabConstraintsDefault.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = GONE
            }
            MODE_INACTIVE -> {
                fabConstraintsDefault.applyTo(rootView)
                button.visibility = GONE
                container.visibility = GONE
            }
            MODE_EDITOR -> {
                fabConstraintsEditor.applyTo(rootView)
                button.visibility = VISIBLE
                container.visibility = VISIBLE
            }
        }
    }
}

