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
import com.bopr.piclock.Settings.Companion.PREF_FLOAT_SPEED
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
) : ContentControlAdapter(settings) {

    private val floatTask = Runnable {
        if (enabled && view.isLaidOut) {
            floatSomewhere {
                scheduleTask(if (isAnimationOn()) 0 else 1000)
            }
        }
    }

    private var floatAnimator: Animator? = null
    private var homeAnimator: Animator? = null
    private var interval = 0L
    private var animated = true

    private var floating = false
        set(value) {
            if (field != value) {
                field = value
                onFloat(field)
            }
        }

    private var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(TAG, "Enabled")

                    scheduleTask(1000) /* let view a second to relayout if needed */
                } else {
                    Log.d(TAG, "Disabled")

                    handler.removeCallbacks(floatTask)
                    cancelAnimators()
                }
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

                Log.d(TAG, "Task scheduled after: $delay")
            } else {
                Log.w(TAG, "Task not scheduled. Interval is negative")
            }
        }
    }

    private fun floatSomewhere(onEnd: () -> Unit) {
        Log.v(TAG, "Moving somewhere")

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
        Log.v(TAG, "Moving home")

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
                    Log.d(TAG, "Start animation to x:$x y:$y")
                }
                doOnEnd {
                    Log.v(TAG, "End animation")

                    onEnd()
                }

                setTarget(view)
                start()
            }
        } else {
            view.x = x
            view.y = y

            Log.d(TAG, "Instantly moved to x:$x y:$y")

            onEnd()
        }
    }

    private fun cancelAnimators() {
        floatAnimator?.cancel()
        homeAnimator?.cancel()
    }

    private fun updateAnimator() {
        cancelAnimators()
        val resId = getResId("animator", settings.getString(PREF_FLOAT_ANIMATION))
        if (resId > 0) {
            floatAnimator = loadAnimator(requireContext(), resId).apply {
                extendProperties(CUSTOM_VIEW_PROPERTIES)
                updateSpeed (settings.getInt(PREF_FLOAT_SPEED))
            }
            homeAnimator = loadAnimator(requireContext(), R.animator.float_home).apply {
                extendProperties(CUSTOM_VIEW_PROPERTIES)
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

    private fun computeAnimationDuration(current: Long): Long {
        return (current * settings.getInt(PREF_FLOAT_SPEED) / 100f).toLong()
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_CONTENT_FLOAT_INTERVAL -> updateInterval()
            PREF_ANIMATION_ON -> updateAnimated()
            PREF_FLOAT_SPEED,
            PREF_FLOAT_ANIMATION -> updateAnimator()
        }
    }

    override fun onModeChanged(animate: Boolean) {
        when (mode) {
            MODE_ACTIVE -> {
                enabled = false
                floatHome()
            }
            MODE_INACTIVE -> {
                enabled = true
            }
            MODE_EDITOR -> {
                enabled = true
            }
        }
    }

    fun pause() {
        Log.v(TAG, "Pause")

        enabled = false
    }

    fun resume() {
        Log.v(TAG, "Resume")

        enabled = true
    }

    companion object {

        private const val TAG = "FloatControl"

        private val CUSTOM_VIEW_PROPERTIES = setOf(
            PROP_X_CURRENT_TO_END,
            PROP_Y_CURRENT_TO_END,
            PROP_ALPHA_CURRENT_TO_ZERO,
            PROP_ALPHA_ZERO_TO_CURRENT
        )

        const val MIN_FLOAT_SPEED = 0
        const val MAX_FLOAT_SPEED = 500
    }
}
