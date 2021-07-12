package com.bopr.piclock

import android.animation.ObjectAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.bopr.piclock.MainFragment.Companion.MODE_ACTIVE
import com.bopr.piclock.MainFragment.Companion.MODE_INACTIVE
import com.bopr.piclock.MainFragment.Mode
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
internal class ScaleControl(private val view: View, private val settings: Settings) :
    ScaleGestureDetector.OnScaleGestureListener {

    private val _tag = "ScaleControl"

    private val gestureDetector by lazy {
        ScaleGestureDetector(view.context, this)
    }

    private val animator by lazy {
        ObjectAnimator.ofFloat(view, PROP_SCALE, 0f).apply {
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
    private var gesturesEnabled = true
    private var defaultScale = 1f

    @Mode
    var mode = MODE_INACTIVE

    private val viewListener =
        OnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                Log.v(_tag, "Layout changed")

                animateTo(computeFitScale(defaultScale), true)
            }
        }

    lateinit var onPinchStart: () -> Unit
    lateinit var onPinch: (scalePercent: Int) -> Unit
    lateinit var onPinchEnd: () -> Unit

    init {
        view.addOnLayoutChangeListener(viewListener)
        updateScale(false)
        updateGesturesState()
    }

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

        defaultScale = viewScale
        settings.update {
            putInt(PREF_CONTENT_SCALE, toPercents(defaultScale))
        }
        onPinchEnd()
        animateTo(computeFitScale(defaultScale), true)
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
            Log.d(_tag, "Start animation")

            animator.apply {
                cancel()
                removeAllListeners()

                setFloatValues(viewScale, scale)
                doOnEnd {
                    Log.d(_tag, "End animation")

                    onEnd()
                }

                start()
            }
        } else {
            viewScale = scale
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

    fun onSettingChanged(key: String) {
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
            gestureDetector.onTouchEvent(event)

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
