package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.property.ScaleProperty
import com.bopr.piclock.util.scaledRect
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control content view scale.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class ScaleControl : ScaleGestureDetector.OnScaleGestureListener {

    private val _tag = "ScaleControl"

    private val detector by lazy {
        ScaleGestureDetector(view.context, this)
    }

    private val rescaleAnimator by lazy {
        ObjectAnimator.ofFloat(view, ScaleProperty(), 0f).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }
    }

    private var viewScale: Float
        get() = view.scaleX
        set(value) {
//            Log.v(_tag, "Set view scale to: $value")

            view.apply {
                scaleX = value
                scaleY = scaleX
            }
        }

    private var pinching = false
    private var rescaling = false
    private var pinchingScale = 0f
    private var scale = 0f

    @Mode
    var mode = MODE_INACTIVE

    private lateinit var view: ViewGroup
    private val viewListener = object : View.OnLayoutChangeListener {

        override fun onLayoutChange(
            v: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                Log.d(_tag, "Layout changed")

                fitViewIntoScreen()
            }
        }

    }

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scalePercent: Int) -> Unit
    lateinit var onPinchEnd: () -> Unit
    lateinit var onScaleChanged: (scalePercent: Int) -> Unit

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        Log.d(_tag, "Start pinching")

        pinching = true
        pinchingScale = viewScale
        onPinchStart()
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        pinchingScale *= detector.scaleFactor
        pinchingScale = max(MIN_FACTOR, min(pinchingScale, MAX_FACTOR))
        viewScale = pinchingScale
        onPinch(percents(pinchingScale))
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(_tag, "End pinching")

        onPinchEnd()
        captureViewScale()
        fitViewIntoScreen()
    }

    private fun captureViewScale() {
        scale = viewScale
        if (mode == MODE_ACTIVE || mode == MODE_INACTIVE) {
            onScaleChanged(percents(scale))
        }
    }

    private fun fitViewIntoScreen(onEnd: () -> Unit = {}) {
        //todo: also move into if out of screen
        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        if (pr.width() < vr.width() || pr.height() < vr.height()) {
            if (!rescaling) {
                Log.d(_tag, "Does not fit. Start rescaling")
                rescaling = true
                val scale = min(pr.width() / view.width, pr.height() / view.height)
                rescaleAnimator.apply {
                    cancel()
                    removeAllListeners()

                    setFloatValues(view.scaleX, scale)
                    doOnEnd {
                        Log.d(_tag, "End rescaling")

                        rescaling = false
                        onEnd()
                    }

                    start()
                }
            }
        }
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (mode == MODE_ACTIVE || mode == MODE_INACTIVE) {
            detector.onTouchEvent(event)

            /* this is to prevent of calling onClick if pinched */
            when (event.action) {
                ACTION_DOWN ->
                    pinching = false
                ACTION_UP ->
                    return pinching
            }
        }

        return false
    }

    fun onModeChanged(mode: Int) {
        this.mode = mode
    }

    fun setView(view: ViewGroup) {
        this.view = view.apply {
            addOnLayoutChangeListener(viewListener)
        }
    }

    fun setScale(valuePercent: Int) {
        scale = factor(valuePercent)
        viewScale = scale
    }

    fun destroy() {
        view.removeOnLayoutChangeListener(viewListener)
    }

    companion object {

        const val MIN_SCALE = 25
        const val MAX_SCALE = 500

        private fun percents(factor: Float) = (factor * 100).toInt()

        private fun factor(percents: Int) = percents / 100f

        private val MIN_FACTOR = factor(MIN_SCALE)
        private val MAX_FACTOR = factor(MAX_SCALE)
    }

}
