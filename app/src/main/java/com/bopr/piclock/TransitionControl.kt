package com.bopr.piclock

import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR

/**
 * Controls transitions between [MainFragment] layouts.
 */
internal class TransitionControl(private val rootView: ViewGroup) {

    private val mainScene: Scene by lazy {
        Scene.getSceneForLayout(rootView, R.layout.fragment_main, rootView.context).apply {

        }
    }

    private val editorScene: Scene by lazy {
        Scene.getSceneForLayout(rootView, R.layout.fragment_main_editor, rootView.context).apply {

        }
    }

    private val transition: Transition by lazy {
        ChangeBounds().apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }
    }

    fun onChangeViewMode(oldMode: Int, newMode: Int) {
//        if (newMode == MODE_ACTIVE) {
//            mainScene.enter()
//        } else if (oldMode == MODE_EDITOR) {
//            editorScene.enter()
//        }

        if (newMode == MODE_EDITOR) {
            TransitionManager.go(editorScene, transition)
        } else if (oldMode == MODE_EDITOR) {
            TransitionManager.go(mainScene, transition)
        }
    }

}
