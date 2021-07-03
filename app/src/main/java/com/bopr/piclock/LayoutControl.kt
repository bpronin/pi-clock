package com.bopr.piclock

import android.util.SparseArray
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

    private val modeConstraints = SparseArray<ConstraintSet>().apply {
        val fabMargin = rootView.context.fabMargin

        append(MODE_ACTIVE, ConstraintSet().apply {
            R.id.settings_container.let {
                connect(it, TOP, R.id.main_container, BOTTOM)
                connect(it, BOTTOM, PARENT_ID, BOTTOM)
                connect(it, RIGHT, PARENT_ID, RIGHT)
                connect(it, LEFT, PARENT_ID, LEFT)
                setVisibility(it, GONE)
            }
            R.id.settings_button.let {
                constrainWidth(it, WRAP_CONTENT)
                constrainHeight(it, WRAP_CONTENT)
                connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
                connect(it, BOTTOM, R.id.main_container, BOTTOM, fabMargin)
                setVisibility(it, VISIBLE)
            }
        })

        append(MODE_INACTIVE, ConstraintSet().apply {
            R.id.settings_container.let {
                connect(it, TOP, R.id.main_container, BOTTOM)
                connect(it, BOTTOM, PARENT_ID, BOTTOM)
                connect(it, RIGHT, PARENT_ID, RIGHT)
                connect(it, LEFT, PARENT_ID, LEFT)
                setVisibility(it, GONE)
            }
            R.id.settings_button.let {
                constrainWidth(it, WRAP_CONTENT)
                constrainHeight(it, WRAP_CONTENT)
                connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
                connect(it, BOTTOM, R.id.main_container, BOTTOM, fabMargin)
                setVisibility(it, GONE)
            }
        })

        append(MODE_EDITOR, ConstraintSet().apply {
            R.id.settings_container.let {
                connect(it, TOP, R.id.main_container, BOTTOM)
                connect(it, BOTTOM, PARENT_ID, BOTTOM)
                connect(it, RIGHT, PARENT_ID, RIGHT)
                connect(it, LEFT, PARENT_ID, LEFT)
                constrainPercentHeight(it, 0.66f)
                setVisibility(it, VISIBLE)
            }
            R.id.settings_button.let {
                constrainWidth(it, WRAP_CONTENT)
                constrainHeight(it, WRAP_CONTENT)
                connect(it, RIGHT, PARENT_ID, RIGHT, fabMargin)
                connect(it, BOTTOM, R.id.main_container, BOTTOM)
                connect(it, TOP, R.id.main_container, BOTTOM)
                setVisibility(it, VISIBLE)
            }
        })
    }

    fun onModeChanged(mode: Int, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView)
        }
        modeConstraints[mode].applyTo(rootView)
    }
}

