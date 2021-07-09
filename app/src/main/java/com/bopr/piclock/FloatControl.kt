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
                    floatAnimator.cancel()
                    homeAnimator.cancel()
                }
            }
        }

    private val viewWrapper = ViewWrapper()
    private lateinit var floatAnimator: Animator
    private lateinit var homeAnimator: Animator

    lateinit var onBusy: (busy: Boolean) -> Unit

    private fun Animator.setValues(endX: Float, endY: Float, startAlpha: Float) {
        /* NOTE: it's not possible to reach PropertyHolder's values initialized in XML nor identify
         animators with ids or tags so we use property names as markers here */
        forEachChild { child ->
            if (child is ObjectAnimator) {
                child.apply {
                    when (propertyName) {
                        "xCurrentToEnd" -> setFloatValues(view.x, endX)
                        "yCurrentToEnd" -> setFloatValues(view.y, endY)
                        "alphaCurrentToZero" -> setFloatValues(startAlpha, 0f)
                        "alphaZeroToCurrent" -> setFloatValues(0f, startAlpha)
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

        val alpha = view.alpha
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

                setValues(x, y, alpha)
                doOnStart {
                    Log.v(_tag, "Start moving somewhere")

                    busy = true
                }
                doOnEnd {
                    Log.v(_tag, "End moving somewhere")

                    view.alpha = alpha /* restore if changed by animator */
                    busy = false
                    onEnd()
                }

                start()
            }
        } else {
            Log.v(_tag, "Moved somewhere")

            view.x = x
            view.y = y
            onEnd()
        }
    }

    private fun floatHome(onEnd: () -> Unit = {}) {
        if (!view.isLaidOut) {
            onEnd()
            return
        }

        val alpha = view.alpha
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

                setValues(x, y, alpha)
                doOnStart {
                    Log.v(_tag, "Start moving home")

                    busy = true
                }
                doOnEnd {
                    Log.v(_tag, "End moving home")

                    view.alpha = alpha /* restore if changed by animator */
                    busy = false
                    onEnd()
                }

                start()
            }
        } else {
            Log.v(_tag, "Moved home")

            view.x = x
            view.y = y
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
