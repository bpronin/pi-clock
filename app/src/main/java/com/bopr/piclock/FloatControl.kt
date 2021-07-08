package com.bopr.piclock

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
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

    private val xAnimator = ObjectAnimator.ofFloat(view, View.X, 0f)
    private val yAnimator = ObjectAnimator.ofFloat(view, View.Y, 0f)
    private val animator = AnimatorSet().apply {
        playTogether(xAnimator, yAnimator)
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

        animator.apply {
            cancel()
            removeAllListeners()

            duration = 10000L
            interpolator = AccelerateDecelerateInterpolator()
            xAnimator.setFloatValues(view.x, x)
            yAnimator.setFloatValues(view.y, y)
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

        animator.apply {
            cancel()
            removeAllListeners()

            duration = 1000L
            interpolator = DecelerateInterpolator()
            xAnimator.setFloatValues(view.x, x)
            yAnimator.setFloatValues(view.y, y)
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

}
