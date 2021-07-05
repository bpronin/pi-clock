package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.property.ScaleProperty
import com.bopr.piclock.util.scaledRect
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control scale.
 */
internal class ScaleControl(private val view: ViewGroup) :
    ScaleGestureDetector.OnScaleGestureListener {

    //todo: individual scale settings for different screen orientation
    private val _tag = "ScaleControl"

    private val detector = ScaleGestureDetector(view.context, this)

    private val rescaleAnimator by lazy {
        ObjectAnimator.ofFloat(view, ScaleProperty(), 0f).apply {
            duration = 700
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
    private var defaultScale = 0f

    private val viewListener = object : OnLayoutChangeListener, OnHierarchyChangeListener {

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
            updateDefaultScale()
        }

        override fun onChildViewAdded(parent: View?, child: View?) {
            updateDefaultScale()
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
            /* do nothing */
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
        updateDefaultScale()
    }

    private fun updateDefaultScale() {
        defaultScale = fitViewScaleIntoScreen()
        onScaleChanged(percents(defaultScale))
    }

    private fun fitViewScaleIntoScreen(): Float {
        //todo: also move into if out of screen
        val pr = view.parentView.scaledRect
        val vr = view.scaledRect
        return if (pr.width() >= vr.width() && pr.height() >= vr.height())
            viewScale
        else {
            val scale = min(pr.width() / view.width, pr.height() / view.height)
            rescaleView(scale)
            return scale
        }
    }

    private fun rescaleView(scale: Float) {
        if (!rescaling) {
            Log.d(_tag, "Start scale animation")

            rescaling = true
            rescaleAnimator.apply {
                cancel()
                removeAllListeners()

                setFloatValues(view.scaleX, scale)
                doOnEnd {
                    Log.d(_tag, "End scale animation")

                    rescaling = false
                }

                start()
            }
        }
    }

    fun init() {
        view.addOnLayoutChangeListener(viewListener)
        view.setOnHierarchyChangeListener(viewListener)
    }

    fun destroy() {
        view.removeOnLayoutChangeListener(viewListener)
        view.setOnHierarchyChangeListener(null)
    }

    fun setDefaultScale(valuePercent: Int) {
        viewScale = factor(valuePercent)
        updateDefaultScale()
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)

        /* this is to prevent of calling onClick if pinched */
        when (event.action) {
            ACTION_DOWN ->
                pinching = false
            ACTION_UP ->
                return pinching
        }

        return false
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
