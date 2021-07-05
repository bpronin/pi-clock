package com.bopr.piclock

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.util.getStringArray
import com.bopr.piclock.util.parentView
import com.bopr.piclock.util.scaledRect
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control scale.
 */
internal class ScaleControl(private val view: View) : ScaleGestureDetector.OnScaleGestureListener {

    //todo: individual scale settings for different screen orientation
    private val _tag = "ScaleControl"

    private val detector = ScaleGestureDetector(view.context, this)

    private val rescaleAnimator by lazy {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, SCALE_X, 0f),
                ObjectAnimator.ofFloat(view, SCALE_Y, 0f)
            )
            duration = 700
            interpolator = DecelerateInterpolator()
        }
//        ObjectAnimator.ofFloat(view, SCALE_X, SCALE_Y, null).apply {
//            duration = 700
//            interpolator = DecelerateInterpolator()
//        }
    }

    private var viewScale: Float
        get() = view.scaleX
        set(value) {
            Log.v(_tag, "Set view scale to: $value")

            view.apply {
                scaleX = value
                scaleY = value
            }
        }

    private var pinching = false
    private var rescaling = false
    private val minScale = view.context.getStringArray(R.array.scale_values).first().toFloat()
    private var pinchingScale = 0f
    private var defaultScale = 0f

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scale: Float) -> Unit
    lateinit var onPinchEnd: () -> Unit
    lateinit var onScaleChanged: (scale: Float) -> Unit

    init {
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            onLayoutChanged()
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        Log.d(_tag, "Start pinching")

        pinching = true
        pinchingScale = viewScale
        onPinchStart()
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        pinchingScale *= detector.scaleFactor
        pinchingScale = max(minScale, min(pinchingScale, MAX_SCALE_FACTOR))
        viewScale = pinchingScale
        onPinch(pinchingScale)
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(_tag, "End pinching")

        onPinchEnd()
        updateDefaultScale()
    }

    private fun updateDefaultScale() {
        defaultScale = fitViewScaleIntoScreen()
        onScaleChanged(defaultScale)
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

                (childAnimations[0] as ObjectAnimator).setFloatValues(view.scaleX, scale)
                (childAnimations[1] as ObjectAnimator).setFloatValues(view.scaleY, scale)
                doOnEnd {
                    Log.d(_tag, "End scale animation")

                    rescaling = false
                }

                start()
            }
        }
    }

    fun setDefaultScale(value: Float) {
        viewScale = value
        updateDefaultScale()
    }

    fun onLayoutChanged() {
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

        private const val MAX_SCALE_FACTOR = 100f
    }

}
