package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.Settings.Companion.PREF_CONTENT_SCALE
import com.bopr.piclock.Settings.Companion.PREF_GESTURES_ENABLED
import com.bopr.piclock.util.*
import com.bopr.piclock.util.property.PROP_SCALE
import kotlin.math.max
import kotlin.math.min

/**
 * Convenience class to control content view scale.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class ScaleControl(private val view: View, settings: Settings) : ContentControl(settings) {

    private val _tag = "ScaleControl"

    private val gestureDetector by lazy {
        GestureDetector(requireContext(), object : SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent?): Boolean {
                pinching = false
                return false
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.v(_tag, "Double tap detected")

                settings.update { putInt(PREF_CONTENT_SCALE, 100) }
                return true
            }
        })
    }

    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(requireContext(), object : SimpleOnScaleGestureListener() {

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                Log.v(_tag, "Start pinching")

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
                Log.v(_tag, "End pinching")

                defaultScale = viewScale
                settings.update {
                    putInt(PREF_CONTENT_SCALE, toPercents(defaultScale))
                }
                onPinchEnd()
                animateTo(computeFitScale(defaultScale), true)
            }
        })
    }

    private val animator by lazy {
        ObjectAnimator.ofFloat(view, PROP_SCALE, 0f).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }
    }

    private val viewListener =
        OnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                val newScale = computeFitScale(defaultScale)
                if (viewScale != newScale) {
                    Log.v(_tag, "Layout changed")

                    animateTo(newScale, true)
                }
            }
        }

    private var viewScale: Float
        get() = view.scaleX
        set(value) {
            Log.d(_tag, "Set view scale to: $value")

            view.apply {
                scaleX = value
                scaleY = scaleX
            }
        }
    private var pinching = false
    private var gesturesEnabled = true
    private var defaultScale = 1f

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scalePercent: Int) -> Unit
    lateinit var onPinchEnd: () -> Unit

    init {
        view.addOnLayoutChangeListener(viewListener)
        updateScale(false)
        updateGesturesState()
    }

    private fun computeFitScale(scale: Float): Float {
        val viewRect = view.rect.scaled(scale)
        val parentRect = view.parentView.scaledRect
        return if (viewRect.width() > parentRect.width() || viewRect.height() > parentRect.height()) {
            min(
                parentRect.width() / view.width,
                parentRect.height() / view.height
            )
        } else {
            scale
        }
    }

    private fun animateTo(scale: Float, animated: Boolean, onEnd: () -> Unit = {}) {
        if (viewScale == scale) return

        if (animated) {
            Log.d(_tag, "Start animation to: $scale")

            animator.apply {
                cancel()
                removeAllListeners()

                setFloatValues(viewScale, scale)
                doOnEnd {
                    Log.v(_tag, "End animation")

                    onEnd()
                }

                start()
            }
        } else {
            viewScale = scale

            Log.d(_tag, "Instantly scaled to: $viewScale")
        }
    }

    private fun updateGesturesState() {
        gesturesEnabled = settings.getBoolean(PREF_GESTURES_ENABLED)
    }

    private fun updateScale(animated: Boolean) {
        defaultScale = toDecimal(settings.getInt(PREF_CONTENT_SCALE))

        /* show actual size first then shrink if needed */
        animateTo(defaultScale, animated) {
            animateTo(computeFitScale(defaultScale), animated)
        }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_CONTENT_SCALE -> updateScale(true)
            PREF_GESTURES_ENABLED -> updateGesturesState()
        }
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (gesturesEnabled && (mode == MODE_ACTIVE || mode == MODE_INACTIVE)) {
            return if (gestureDetector.onTouchEvent(event)) {
                true
            } else {
                scaleGestureDetector.onTouchEvent(event)
                pinching
            }
        }
        return false
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
