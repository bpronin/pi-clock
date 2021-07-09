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
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.forEachChild
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.rect
import com.bopr.piclock.util.scaledRect
import java.lang.Math.random

/**
 * Convenience class to control floating content view along the screen.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class FloatControl(private val view: View, private val handler: Handler) {

    private val _tag = "FloatControl"

    private var interval = 0L
    private var animated: Boolean = true
    private var animating = false
        set(value) {
            if (field != value) {
                field = value
                onAnimate(field)
            }
        }

    private val floatTask = Runnable {
        if (enabled && view.isLaidOut) {
            floatSomewhere {
                scheduleTask(if (isAnimationOn()) 0 else 1000)
            }
        }
    }

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

    private val viewWrapper by lazy { ViewWrapper() }

    private var floatAnimator: Animator? = null
    private var homeAnimator: Animator? = null

    lateinit var onAnimate: (animating: Boolean) -> Unit

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

        val alpha = view.alpha
        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        val dw = pr.width() - vr.width()
        val dh = pr.height() - vr.height()
        val dx = view.x - vr.left
        val dy = view.y - vr.top
        val x = random().toFloat() * dw + dx
        val y = random().toFloat() * dh + dy

        runAnimator(floatAnimator, x, y, alpha, onEnd)
    }

    private fun floatHome() {
        Log.v(_tag, "Moving home")

        val alpha = view.alpha
        val pr = view.parentView.rect
        val vr = view.rect
        val x = (pr.width() - vr.width()) / 2
        val y = (pr.height() - vr.height()) / 2

        runAnimator(homeAnimator, x, y, alpha, {})
    }

    private fun runAnimator(
        animator: Animator?,
        x: Float,
        y: Float,
        alpha: Float,
        onEnd: () -> Unit
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
                                "xCurrentToEnd" -> setFloatValues(view.x, x)
                                "yCurrentToEnd" -> setFloatValues(view.y, y)
                                "alphaCurrentToZero" -> setFloatValues(view.alpha, 0f)
                                "alphaZeroToCurrent" -> setFloatValues(0f, view.alpha)
                            }
                        }
                    }
                }

                removeAllListeners()
                doOnStart {
                    Log.v(_tag, "Start animation")

                    animating = true
                }
                doOnEnd {
                    Log.v(_tag, "End animation")

                    view.alpha = alpha /* restore if changed during animation */
                    animating = false
                    onEnd()
                }

                setTarget(viewWrapper)
                start()
            }
        } else {
            view.x = x
            view.y = y

            Log.v(_tag, "Moved immediately")

            onEnd()
        }
    }

    private fun cancelAnimators() {
        floatAnimator?.cancel()
        homeAnimator?.cancel()
    }

    fun setAnimator(resId: Int) {
        cancelAnimators()
        if (resId > 0) {
            floatAnimator = loadAnimator(view.context, resId)
            homeAnimator = loadAnimator(view.context, R.animator.float_home)
        } else {
            floatAnimator = null
            homeAnimator = null
        }
    }

    fun setAnimated(value: Boolean) {
        animated = value
        if (!animated) {
            cancelAnimators()
        }
    }

    fun setInterval(value: Long) {
        interval = value
    }

    fun onModeChanged(@Mode mode: Int) {
        when (mode) {
            MODE_ACTIVE -> {
                enabled = false
                floatHome()
            }
            MODE_INACTIVE -> {
                enabled = true
            }
            MODE_EDITOR -> {
                enabled = false
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

    @Suppress("unused")
    private inner class ViewWrapper {

        var x
            get() = view.x
            set(value) {
                view.x = value
            }

        var y
            get() = view.y
            set(value) {
                view.y = value
            }

        var alpha
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

        var xCurrentToEnd by ::x
        var yCurrentToEnd by ::y
        var alphaCurrentToZero by ::alpha
        var alphaZeroToCurrent by ::alpha

    }
}
