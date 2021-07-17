package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
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
internal class ScaleControl(private val view: View, settings: Settings) : ContentControlAdapter(settings),
    Destroyable {

    private val gestureDetector by lazy {
        GestureDetector(requireContext(), object : SimpleOnGestureListener() {

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.v(TAG, "Double tap detected")

                viewScale = 1f
                saveScale()
                return true
            }
        })
    }

    private val scaleGestureDetector by lazy {
        ScaleGestureDetector(requireContext(), object : SimpleOnScaleGestureListener() {

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                Log.v(TAG, "Start pinching")

                pinched = true
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
                Log.v(TAG, "End pinching")

                saveScale()
                onPinchEnd()
                animateTo(computeFitScale(savedScale))
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
                val newScale = computeFitScale(savedScale)
                if (viewScale != newScale) {
                    Log.v(TAG, "Layout changed")

                    animateTo(newScale)
                }
            }
        }

    private var viewScale: Float
        get() = view.scaleX
        set(value) {
//            Log.v(TAG, "Set view scale to: $value")

            view.apply {
                scaleX = value
                scaleY = scaleX
            }
        }
    private var pinched = false
    private var gesturesEnabled = true
    private var savedScale = 1f

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scalePercent: Int) -> Unit
    lateinit var onPinchEnd: () -> Unit

    init {
        loadScale(false)
        loadGesturesState()
        view.addOnLayoutChangeListener(viewListener)
    }

    override fun destroy() {
        view.removeOnLayoutChangeListener(viewListener)
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

    private fun animateTo(scale: Float, onEnd: () -> Unit = {}) {
        if (viewScale == scale) return

        Log.d(TAG, "Start animation to: $scale")

        animator.apply {
            cancel()
            removeAllListeners()

            setFloatValues(viewScale, scale)
            doOnEnd {
                Log.v(TAG, "End animation")

                onEnd()
            }

            start()
        }
    }

    private fun loadGesturesState() {
        gesturesEnabled = settings.getBoolean(PREF_GESTURES_ENABLED)
    }

    private fun loadScale(animated: Boolean) {
        savedScale = toDecimal(settings.getInt(PREF_CONTENT_SCALE))

        if (animated) {
            animateTo(savedScale) { /* first show actual size*/
                animateTo(computeFitScale(savedScale)) /* then fit into screen */
            }
        } else {
            viewScale = savedScale

            Log.d(TAG, "Instantly scaled to: $viewScale")
        }
    }

    private fun saveScale() {
        savedScale = viewScale
        settings.update { putInt(PREF_CONTENT_SCALE, toPercents(savedScale)) }
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_CONTENT_SCALE -> loadScale(true)
            PREF_GESTURES_ENABLED -> loadGesturesState()
        }
    }

    /**
     * To be called in owner's onTouch.
     */
    fun onTouch(event: MotionEvent): Boolean {
        if (gesturesEnabled && (mode == MODE_ACTIVE || mode == MODE_INACTIVE)) {
            if (gestureDetector.onTouchEvent(event)) return true

            /* this prevents from calling onClick when pinched */
            scaleGestureDetector.onTouchEvent(event)
            when (event.action) {
                ACTION_DOWN -> pinched = false
                ACTION_UP -> return pinched
            }
        }

        return false
    }

    companion object {

        private const val TAG = "ScaleControl"

        const val MIN_SCALE = 25
        const val MAX_SCALE = 500

        private val MIN_FACTOR = toDecimal(MIN_SCALE)
        private val MAX_FACTOR = toDecimal(MAX_SCALE)
    }

}
