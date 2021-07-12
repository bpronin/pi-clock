package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.ObjectAnimator
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_FLOAT_INTERVAL
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_ANIMATION
import com.bopr.piclock.util.*
import com.bopr.piclock.util.property.PROP_ALPHA_CURRENT_TO_ZERO
import com.bopr.piclock.util.property.PROP_ALPHA_ZERO_TO_CURRENT
import com.bopr.piclock.util.property.PROP_X_CURRENT_TO_END
import com.bopr.piclock.util.property.PROP_Y_CURRENT_TO_END
import java.lang.Math.random

/**
 * Convenience class to control floating content view along the screen.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class FloatControl(
    private val view: View,
    private val handler: Handler,
    settings: Settings
) : ContentControl(settings) {

    private val _tag = "FloatControl"
    private val floatTask = Runnable {
        if (enabled && view.isLaidOut) {
            floatSomewhere {
                scheduleTask(if (isAnimationOn()) 0 else 1000)
            }
        }
    }
    private val customViewProperties = setOf(
        PROP_X_CURRENT_TO_END,
        PROP_Y_CURRENT_TO_END,
        PROP_ALPHA_CURRENT_TO_ZERO,
        PROP_ALPHA_ZERO_TO_CURRENT
    )

    private var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Enabled")

                    scheduleTask(1000) /* let view a second to relayout if needed */
                } else {
                    Log.d(_tag, "Disabled")

                    handler.removeCallbacks(floatTask)
                    cancelAnimators()
                }
            }
        }
    private var floatAnimator: Animator? = null
    private var homeAnimator: Animator? = null
    private var interval = 0L
    private var animated: Boolean = true
    private var floating = false
        set(value) {
            if (field != value) {
                field = value
                onFloat(field)
            }
        }

    lateinit var onFloat: (animating: Boolean) -> Unit

    init {
        updateInterval()
        updateAnimator()
        updateAnimated()
    }

    private fun isAnimationOn() = animated && floatAnimator != null && homeAnimator != null

    private fun scheduleTask(startDelay: Long = 0) {
        if (enabled) {
            if (interval >= 0) {
                val delay = interval + startDelay
                handler.postDelayed(floatTask, delay)

                Log.d(_tag, "Task scheduled after: $delay")
            } else {
                Log.w(_tag, "Task not scheduled. Interval is negative")
            }
        }
    }

    private fun floatSomewhere(onEnd: () -> Unit) {
        Log.v(_tag, "Moving somewhere")

        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        val dw = pr.width() - vr.width()
        val dh = pr.height() - vr.height()
        val dx = view.x - vr.left
        val dy = view.y - vr.top
        val x = random().toFloat() * dw + dx
        val y = random().toFloat() * dh + dy

        floating = true
        runAnimator(floatAnimator, x, y) {
            floating = false
            onEnd()
        }
    }

    private fun floatHome() {
        Log.v(_tag, "Moving home")

        val pr = view.parentView.rect
        val vr = view.rect
        val x = (pr.width() - vr.width()) / 2
        val y = (pr.height() - vr.height()) / 2

        floating = false
        runAnimator(homeAnimator, x, y)
    }

    private fun runAnimator(
        animator: Animator?,
        x: Float,
        y: Float,
        onEnd: () -> Unit = {}
    ) {
        cancelAnimators()

        if (x == view.x && y == view.y) {
            onEnd()
            return
        }

        if (isAnimationOn()) {
            animator?.run {
                /* NOTE: it's not possible to reach PropertyHolder's values initialized in XML nor identify
                   animators with ids or tags so we use property names as markers here */
                forEachChild { child ->
                    if (child is ObjectAnimator) {
                        child.apply {
                            when (propertyName) {
                                PROP_X_CURRENT_TO_END.name -> setFloatValues(view.x, x)
                                PROP_Y_CURRENT_TO_END.name -> setFloatValues(view.y, y)
                                PROP_ALPHA_CURRENT_TO_ZERO.name -> setFloatValues(view.alpha, 0f)
                                PROP_ALPHA_ZERO_TO_CURRENT.name -> setFloatValues(0f, view.alpha)
                            }
                        }
                    }
                }

                removeAllListeners()
                doOnStart {
                    Log.d(_tag, "Start animation to x:$x y:$y")
                }
                doOnEnd {
                    Log.v(_tag, "End animation")

                    onEnd()
                }

                setTarget(view)
                start()
            }
        } else {
            view.x = x
            view.y = y

            Log.d(_tag, "Instantly moved to x:$x y:$y")

            onEnd()
        }
    }

    private fun cancelAnimators() {
        floatAnimator?.cancel()
        homeAnimator?.cancel()
    }

    private fun updateAnimator() {
        cancelAnimators()
        val resId = view.context.getResId("animator", settings.getString(PREF_FLOAT_ANIMATION))
        if (resId > 0) {
            floatAnimator = loadAnimator(view.context, resId).apply {
                extendProperties(customViewProperties)
            }
            homeAnimator = loadAnimator(view.context, R.animator.float_home).apply {
                extendProperties(customViewProperties)
            }
        } else {
            floatAnimator = null
            homeAnimator = null
        }
    }

    private fun updateInterval() {
        interval = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)
    }

    private fun updateAnimated() {
        animated = settings.getBoolean(PREF_ANIMATION_ON)
        if (!animated) {
            cancelAnimators()
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_CONTENT_FLOAT_INTERVAL -> updateInterval()
            PREF_FLOAT_ANIMATION -> updateAnimator()
            PREF_ANIMATION_ON -> updateAnimated()
        }
    }

    override fun onModeChanged(newMode: Int, animate: Boolean) {
        super.onModeChanged(newMode, animate)
        when (newMode) {
            MODE_ACTIVE -> {
                enabled = false
                floatHome()
            }
            MODE_INACTIVE -> {
                enabled = true
            }
            MODE_EDITOR -> {
                enabled = false
                //todo: enable and move faster to show the chosen animation
            }
        }
    }

    fun pause() {
        Log.v(_tag, "Pause")

        enabled = false
    }

    fun resume() {
        Log.v(_tag, "Resume")

        enabled = true
    }

}
