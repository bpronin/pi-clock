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
    private var busy = false
        private set(value) {
            if (field != value) {
                field = value
                onBusy(field)
            }
        }

    lateinit var onBusy: (busy: Boolean) -> Unit

    private val task = Runnable {
        if (enabled) floatSomewhere {
            scheduleTask()
        }
    }

    private var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    Log.d(_tag, "Enabled")

                    scheduleTask(1000)
                } else {
                    Log.d(_tag, "Disabled")

                    handler.removeCallbacks(task)
                }
            }
        }

    private val viewWrapper = ViewWrapper()
    private lateinit var somewhereAnimator: Animator
    private lateinit var homeAnimator: Animator

    private fun Animator.setup(endX: Float, endY: Float) {
        /* NOTE: it is not possible to reach PropertyHolder values set in XML so we use
           property names as markers */
        forEachChild { animator ->
            if (animator is ObjectAnimator) {
                animator.apply {
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
        }
        setTarget(viewWrapper)
    }

    private fun scheduleTask(startDelay: Long = 0) {
        if (enabled) {
            when {
                interval == 0L -> {
                    handler.postDelayed(task, startDelay)

                    Log.d(_tag, "Task posted now")
                }
                interval > 0 -> {
                    handler.postDelayed(task, interval + startDelay)

                    Log.d(_tag, "Task scheduled after: $interval")
                }
                else -> {
                    Log.v(_tag, "Task not scheduled. interval: $interval")
                }
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

        homeAnimator.cancel()
        somewhereAnimator.apply {
            cancel()
            removeAllListeners()

            setup(x, y)
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

        somewhereAnimator.cancel()
        homeAnimator.apply {
            cancel()
            removeAllListeners()

            setup(x, y)
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
    }

    fun setAnimator(resId: Int) {
        somewhereAnimator = loadAnimator(view.context, resId)
        homeAnimator = loadAnimator(view.context, R.animator.float_home)
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

        var xCurrentToEnd 
            get() = view.x
            set(value) {
                view.x = value
            }

        var yCurrentToEnd
            get() = view.y
            set(value) {
                view.y = value
            }

        var alphaCurrentTo00
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

        var alpha00ToCurrent
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

        var alphaCurrentTo01
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

        var alpha01ToCurrent
            get() = view.alpha
            set(value) {
                view.alpha = value
            }

    }
}
