package com.bopr.piclock

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_LIGHT
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.util.Log
import android.view.View

/**
 * Convenience class to change brightness depending on ambient light.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class IlluminanceControl(context: Context) {

    private val _tag = "IlluminanceControl"

    private lateinit var view: View
    private var illuminance = MAX_ILLUMINANCE
        set(value) {
            if (field != value) {
                field = value

                Log.d(_tag, "Illuminance set to: $field")

                updateViewBrightness()
            }
        }
    private var enabled = true

    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(TYPE_LIGHT)
    private val sensorListener = object : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            /* do nothing */
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (enabled) {
                event?.values?.also {
                    illuminance = it[0] / sensor.maximumRange
                }
            }
        }

    }.also {
        sensorManager.registerListener(it, sensor, SENSOR_DELAY_NORMAL)
    }

    lateinit var onIlluminanceChange: (illuminance: Float) -> Unit

    private fun updateViewBrightness() {
        onIlluminanceChange(illuminance)
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled

        Log.d(_tag, "Enabled: $enabled")

        if (!this.enabled) {
            illuminance = MAX_ILLUMINANCE
        }
    }

    fun setView(view: View) {
        this.view = view
    }

    fun destroy() {
        sensorManager.unregisterListener(sensorListener)
    }

    companion object {

        const val MIN_ILLUMINANCE = 0f
        const val MAX_ILLUMINANCE = 1f
    }

}
