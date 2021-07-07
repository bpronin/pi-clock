package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_EDITOR
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.property.ScaleProperty
import com.bopr.piclock.util.scaledRect
import com.bopr.piclock.util.toDecimal
import com.bopr.piclock.util.toPercents
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
    private var defaultScale = 1f

    @Mode
    var mode = MODE_INACTIVE

    private lateinit var view: ViewGroup
    private val viewListener =
        OnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                Log.d(_tag, "Layout changed")

                fitViewIntoScreen()
            }
        }

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scalePercent: Int) -> Unit
    lateinit var onPinchEnd: () -> Unit
    lateinit var onScaleChanged: (scalePercent: Int) -> Unit

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        Log.d(_tag, "Start pinching")

        pinching = true
        onPinchStart()
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scale = viewScale * detector.scaleFactor
        viewScale = max(MIN_FACTOR, min(scale, MAX_FACTOR))
        onPinch(toPercents(viewScale))
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(_tag, "End pinching")

        onPinchEnd()
        captureViewScale()
        fitViewIntoScreen()
    }

    private fun captureViewScale() {
        defaultScale = viewScale
        if (mode == MODE_ACTIVE || mode == MODE_INACTIVE) {
            onScaleChanged(toPercents(defaultScale))
        }
    }

    private fun fitViewIntoScreen() {
        //todo: also move into if out of screen
        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        if (pr.width() < vr.width() || pr.height() < vr.height()) {
            Log.d(_tag, "Does not fit. Start rescaling")

            val scale = min(pr.width() / view.width, pr.height() / view.height)
            animateRescale(scale)
        }
    }

    private fun animateRescale(scale: Float) {
        rescaleAnimator.apply {
            cancel()
            removeAllListeners()

            setFloatValues(viewScale, scale)
            doOnEnd {
                Log.d(_tag, "End rescaling")
            }

            start()
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

    fun onModeChanged(value: Int) {
        mode = value
        if (mode == MODE_EDITOR) {
            animateRescale(1f)
        } else {
            animateRescale(defaultScale)
        }
    }

    fun setView(value: ViewGroup) {
        view = value.apply {
            addOnLayoutChangeListener(viewListener)
        }
    }

    fun setScale(valuePercent: Int) {
        defaultScale = toDecimal(valuePercent)
        viewScale = defaultScale
    }

    fun destroy() {
        view.removeOnLayoutChangeListener(viewListener)
    }

    companion object {

        const val MIN_SCALE = 25
        const val MAX_SCALE = 500

        private val MIN_FACTOR = toDecimal(MIN_SCALE)
        private val MAX_FACTOR = toDecimal(MAX_SCALE)
    }

}
