package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
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
 * Controls content view floating movement along the screen.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class FloatControl(
    private val view: View, settings: Settings
) : ContentControlAdapter(settings), Destroyable {

    private val handler = Handler(Looper.getMainLooper())

    private val floatTask = {
        if (enabled) {
            Log.v(TAG, "Running task")

            floatSomewhere {
                scheduleTask()
            }
        }
    }

    private val canAnimate get() = animationOn && floatAnimator != null && homeAnimator != null

    private var floatAnimator: Animator? = null
    private var homeAnimator: Animator? = null
    private var interval = 0L
    private var animationOn = true

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

                    scheduleTask()
                } else {
                    Log.d(TAG, "Disabled")

                    cancelTask()
                    cancelAnimators()
                }
            }
        }

    lateinit var onFloat: (animating: Boolean) -> Unit

    init {
        updateInterval()
        updateAnimators()
        updateAnimationOn()
    }

    override fun destroy() {
        cancelTask()
    }

    private fun scheduleTask() {
        if (!enabled || interval < 0) return

        val delay = if (animationOn || interval > 0) {
            interval
        } else {
            /* prevent content from jumping along the screen when animations
            is disabled ans interval is 0 */
            SECOND_DURATION
        }

        handler.postDelayed(floatTask, delay)

        Log.d(TAG, "Task scheduled in: $delay")
    }

    private fun cancelTask() {
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Task canceled")
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

    private fun floatHome(onEnd: () -> Unit = {}) {
        Log.v(TAG, "Moving home")

        val pr = view.parentView.rect
        val vr = view.rect
        val x = (pr.width() - vr.width()) / 2
        val y = (pr.height() - vr.height()) / 2

        runAnimator(homeAnimator, x, y) {
            onEnd()
        }
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

        if (canAnimate) {
            animator?.run {
                Log.v(TAG, "Start animation to x:$x y:$y")

                /* NOTE: it's not possible to reach PropertyHolder's values initialized in XML nor identify
                   animators with ids or tags so we use property names as markers here */
                forEachChildRecursively { child ->
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
                doOnEnd {
                    Log.v(TAG, "End animation")

                    onEnd()
                }

                setTarget(view)
                start()
            }
        } else {
            Log.v(TAG, "Instantly moved to x:$x y:$y")

            view.x = x
            view.y = y
            onEnd()
        }
    }

    private fun cancelAnimators() {
        floatAnimator?.cancel()
        homeAnimator?.cancel()
    }

    private fun updateAnimators() {
        cancelAnimators()
        val animationName = settings.getString(PREF_FLOAT_ANIMATION)
        if (animationName.isNotEmpty()) {
            val resId = requireAnimatorResId(animationName)
            floatAnimator = loadAnimator(requireContext(), resId).apply {
                extendProperties(CUSTOM_VIEW_PROPERTIES)
                updateSpeed(settings.getInt(PREF_FLOAT_SPEED))
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
        cancelTask()
        interval = settings.getLong(PREF_CONTENT_FLOAT_INTERVAL)

        Log.v(TAG, "Interval set to :$interval")

        if (interval < 0) floatHome() else scheduleTask()
    }

    private fun updateAnimationOn() {
        animationOn = settings.getBoolean(PREF_ANIMATION_ON)
        if (!animationOn) {
            cancelAnimators()
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_CONTENT_FLOAT_INTERVAL -> updateInterval()
            PREF_ANIMATION_ON -> updateAnimationOn()
            PREF_FLOAT_SPEED,
            PREF_FLOAT_ANIMATION -> updateAnimators()
        }
    }

    override fun onModeChanged(animate: Boolean) {
        when (mode) {
            MODE_ACTIVE -> {
                enabled = false
                floatHome()
            }
            MODE_INACTIVE,
            MODE_EDITOR -> {
                enabled = false
                /* wait a second for layout animations finished */
                handler.postDelayed({
                    floatHome {
                        enabled = true
                    }
                }, SECOND_DURATION)
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

    }

}
