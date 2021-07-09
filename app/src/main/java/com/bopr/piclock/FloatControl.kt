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
    private var busy = false
        set(value) {
            if (field != value) {
                field = value
                onBusy(field)
            }
        }

    private val floatTask = Runnable {
        if (enabled) floatSomewhere {
            scheduleTask(if (animated) 0 else 1000)
        }
    }

    private var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Enabled")

                    scheduleTask(1000) /* let a second to relayout if needed */
                } else {
                    Log.d(_tag, "Disabled")

                    handler.removeCallbacks(floatTask)
                }
            }
        }

    private val viewWrapper = ViewWrapper()
    private lateinit var floatAnimator: Animator
    private lateinit var homeAnimator: Animator

    lateinit var onBusy: (busy: Boolean) -> Unit

    private fun Animator.setValues(endX: Float, endY: Float) {
        /* NOTE: it is not possible to reach PropertyHolder's values initialized in XML so we use
           property names as markers here */
        forEachChild { child ->
            if (child is ObjectAnimator) {
                child.apply {
                    when (propertyName) {
                        "xCurrentToEnd" -> setFloatValues(view.x, endX)
                        "yCurrentToEnd" -> setFloatValues(view.y, endY)
                        "alphaCurrentTo00" -> setFloatValues(view.alpha, 0f)
                        "alpha00ToCurrent" -> setFloatValues(0f, view.alpha)
                        "alphaCurrentTo01" -> setFloatValues(view.alpha, 0.1f)
                        "alpha01ToCurrent" -> setFloatValues(0.1f, view.alpha)
                    }
                }
            }
            setTarget(viewWrapper)
        }
    }

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
        if (!view.isLaidOut) {
            onEnd()
            return
        }

        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        val dw = pr.width() - vr.width()
        val dh = pr.height() - vr.height()
        val dx = view.x - vr.left
        val dy = view.y - vr.top
        val x = random().toFloat() * dw + dx
        val y = random().toFloat() * dh + dy

        if (animated) {
            homeAnimator.cancel()
            floatAnimator.apply {
                cancel()
                removeAllListeners()

                setValues(x, y)
                doOnStart {
                    Log.v(_tag, "Start moving somewhere")

                    busy = true
                }
                doOnEnd {
                    Log.v(_tag, "End moving somewhere")

                    busy = false
                    onEnd()
                }

                start()
            }
        } else {
            view.x = x
            view.y = y

            Log.v(_tag, "Moved somewhere")

            onEnd()
        }
    }

    private fun floatHome(onEnd: () -> Unit = {}) {
        if (!view.isLaidOut) {
            onEnd()
            return
        }

        val pr = view.parentView.rect
        val vr = view.rect
        val x = (pr.width() - vr.width()) / 2
        val y = (pr.height() - vr.height()) / 2

        if (x == view.x || y == view.y) {
            onEnd()
            return
        }

        if (animated) {
            floatAnimator.cancel()
            homeAnimator.apply {
                cancel()
                removeAllListeners()

                setValues(x, y)
                doOnStart {
                    Log.v(_tag, "Start moving home")

                    busy = true
                }
                doOnEnd {
                    Log.v(_tag, "End moving home")

                    busy = false
                    onEnd()
                }

                start()
            }
        } else {
            view.x = x
            view.y = y

            Log.v(_tag, "Moved home")

            onEnd()
        }
    }

    fun setAnimator(resId: Int) {
        floatAnimator = loadAnimator(view.context, resId)
        homeAnimator = loadAnimator(view.context, R.animator.float_home)
    }

    fun setAnimated(value: Boolean) {
        animated = value
        if (!animated) {
            floatAnimator.cancel()
            homeAnimator.cancel()
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

    fun onPause() {
        Log.v(_tag, "Pause")

        enabled = false
    }

    fun onResume() {
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
        var alphaCurrentTo00 by ::alpha
        var alpha00ToCurrent by ::alpha
        var alphaCurrentTo01 by ::alpha
        var alpha01ToCurrent by ::alpha

    }
}
