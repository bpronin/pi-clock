package com.bopr.piclock

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.ROTATION
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bopr.piclock.Settings.Companion.PREF_ANIMATION_ON
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_HAND_ANIMATION
import com.bopr.piclock.Settings.Companion.PREF_CLOCK_HAND_MOVE_SMOOTH
import com.bopr.piclock.Settings.Companion.PREF_SECOND_HAND_VISIBLE
import com.bopr.piclock.util.forEachChildRecursively
import com.bopr.piclock.util.getAnimatorResId
import java.util.*
import java.util.Calendar.*

/**
 * Controls analog clock representation of the content view.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AnalogClockControl(private val view: View, settings: Settings) :
    ContentControlAdapter(settings) {

// todo:  часы наизнанку - стрелки прикреплены к ободу а не к центру
// todo:  вместо цифр доли числа Пи (фирменный стиль)
// todo:  clock face with 24 hours
// todo:  fade in-out handles animation
// todo:  rounder hands style

    private val textDateViewControl = AnalogClockTextDateControl(view, settings)
    private val barsDateControl = AnalogClockBarsDateControl(view, settings)
    private val hourHandView: ImageView by lazy { view.findViewById(R.id.hour_hand_view) }
    private val secondHandView: ImageView by lazy { view.findViewById(R.id.second_hand_view) }
    private val minuteHandView: ImageView by lazy { view.findViewById(R.id.minute_hand_view) }
    private val canAnimate get() = animationOn && secondHandAnimator != null

    private var hoursHandAnimator: Animator? = null
    private var minuteHandAnimator: Animator? = null
    private var secondHandAnimator: Animator? = null
    private var animationOn = true
    private var smoothOn = false
    private var currentTime = Date()

    init {
        updateAnimationOn()
        updateSmoothOn()
        updateSecondHandView()
        updateViewsData(false)
        updateAnimators()
    }

    private fun updateAnimators() {
        cancelAnimators()
        val resId = getAnimatorResId(settings.getString(PREF_CLOCK_HAND_ANIMATION))
        if (resId != 0) {
            secondHandAnimator = loadAnimator(requireContext(), resId).apply {
                setTarget(secondHandView)
            }
            minuteHandAnimator = loadAnimator(requireContext(), resId).apply {
                setTarget(minuteHandView)
            }
            hoursHandAnimator = loadAnimator(requireContext(), resId).apply {
                setTarget(hoursHandAnimator)
            }
        } else {
            secondHandAnimator = null
            minuteHandAnimator = null
            hoursHandAnimator = null
        }
    }

    private fun updateSecondHandView() {
        secondHandView.isGone = !settings.getBoolean(PREF_SECOND_HAND_VISIBLE)
    }

    private fun updateAnimationOn() {
        animationOn = settings.getBoolean(PREF_ANIMATION_ON)
        if (animationOn) {
            cancelAnimators()
        }
    }

    private fun updateSmoothOn() {
        smoothOn = settings.getBoolean(PREF_CLOCK_HAND_MOVE_SMOOTH)
        updateViewsData(false)
    }

    private fun cancelAnimators() {
        secondHandAnimator?.cancel()
        minuteHandAnimator?.cancel()
        hoursHandAnimator?.cancel()
    }

    private fun updateViewsData(animated: Boolean) {
        getInstance().apply {
            time = currentTime

            var hourAngle = get(HOUR) * 30f /* not HOUR-OF_DAY */
            var minuteAngle = get(MINUTE) * 6f
            val secondAngle = get(SECOND) * 6f
            if (smoothOn) {
                minuteAngle += secondAngle / 60f
                hourAngle += minuteAngle / 60f
            }

            rotateHand(hourHandView, hourAngle, hoursHandAnimator, animated && !smoothOn)
            rotateHand(minuteHandView, minuteAngle, minuteHandAnimator, animated && !smoothOn)
            rotateHand(secondHandView, secondAngle, secondHandAnimator, animated)
        }
    }

    private fun rotateHand(handView: View, angle: Float, animator: Animator?, animated: Boolean) {
        if (!handView.isVisible || handView.rotation == angle) return

        if (animated) {
            animator?.apply {
                end() /* not cancel here */

                var start = handView.rotation
                var end = angle
                if (start > 360) start -= 360
                if (start > end) end += 360

                forEachChildRecursively { child ->
                    if (child is ObjectAnimator) {
                        child.apply {
                            if (propertyName == ROTATION.name) setFloatValues(start, end)
                        }
                    }
                }

                start()
            }
        } else {
            handView.rotation = angle
        }
    }

    override fun onTimer(time: Date, tick: Int) {
        currentTime = time
        if (tick == 1) updateViewsData(canAnimate)
        barsDateControl.onTimer(time, tick)
        textDateViewControl.onTimer(time, tick)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimationOn()
            PREF_CLOCK_HAND_ANIMATION -> updateAnimators()
            PREF_CLOCK_HAND_MOVE_SMOOTH -> updateSmoothOn()
            PREF_SECOND_HAND_VISIBLE -> {
                updateSecondHandView()
                updateViewsData(canAnimate)
            }
        }
        barsDateControl.onSettingChanged(key)
        textDateViewControl.onSettingChanged(key)
    }

    companion object {

        fun isAnalogClockLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_analog")
        }

        fun isAnalogClockBarsDateLayout(layoutName: String): Boolean {
            return layoutName.startsWith("view_analog_bars_date")
        }
    }
}