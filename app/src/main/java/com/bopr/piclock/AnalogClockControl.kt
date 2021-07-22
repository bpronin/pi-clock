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
import com.bopr.piclock.Settings.Companion.PREF_SECOND_HAND_VISIBLE
import com.bopr.piclock.util.forEachChildRecusively
import com.bopr.piclock.util.getResId
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

    private val hourHandView: ImageView by lazy { view.findViewById(R.id.hour_hand_view) }
    private val secondHandView: ImageView by lazy { view.findViewById(R.id.second_hand_view) }
    private val minuteHandView: ImageView by lazy { view.findViewById(R.id.minute_hand_view) }
    private val textDateViewControl = AnalogClockTextDateControl(view, settings)
    private val barsDateControl = AnalogClockBarsDateControl(view, settings)
    private val canAnimate get() = animationOn && secondHandAnimator != null

    private var hoursHandAnimator: Animator? = null
    private var minuteHandAnimator: Animator? = null
    private var secondHandAnimator: Animator? = null
    private var animationOn = true
    private var currentTime = Date()

    init {
        updateAnimationOn()
        updateSecondHandView()
        updateViewsData(false)
        updateAnimators()
    }

    private fun updateAnimators() {
        cancelAnimators()
        val resId = getResId("animator", settings.getString(PREF_CLOCK_HAND_ANIMATION))
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

    private fun cancelAnimators() {
        secondHandAnimator?.cancel()
        minuteHandAnimator?.cancel()
        hoursHandAnimator?.cancel()
    }

    private fun updateViewsData(animated: Boolean) {
        GregorianCalendar.getInstance().apply {
            time = currentTime

            minuteHandView.rotation = get(MINUTE) * 6f
            hourHandView.rotation = get(HOUR) * 30f
            rotateHand(secondHandView, get(SECOND) * 6f, secondHandAnimator, animated)
        }
    }

    private fun rotateHand(handView: View, angle: Float, animator: Animator?, animated: Boolean) {
        if (!handView.isVisible) return

        if (animated) {
            animator?.apply {
                cancel()

                val start = if (handView.rotation <= 360f)
                    handView.rotation
                else
                    handView.rotation - 360f

                val end = if (start <= angle) angle else angle + 360f

                forEachChildRecusively { child ->
                    if (child is ObjectAnimator) {
                        child.apply {
                            when (propertyName) {
                                ROTATION.name -> setFloatValues(start, end)
                            }
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
        barsDateControl.onTimer(currentTime, tick)
        textDateViewControl.onTimer(currentTime, tick)
    }

    override fun onSettingChanged(key: String) {
        when (key) {
            PREF_ANIMATION_ON -> updateAnimationOn()
            PREF_CLOCK_HAND_ANIMATION -> updateAnimators()
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