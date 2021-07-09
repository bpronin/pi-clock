package com.bopr.piclock

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_LIGHT
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL

/**
 * Convenience class to work with ambient light.
 *
 * @author Boris P. ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class IlluminanceDetector(context: Context) {

    private val _tag = "IlluminanceDetector"

    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(TYPE_LIGHT)
    private val sensorListener = object : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            /* do nothing */
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.also { values ->
                onIlluminanceChange(values[0] / sensor.maximumRange)
            }
        }

    }.also {
        sensorManager.registerListener(it, sensor, SENSOR_DELAY_NORMAL)
    }

    lateinit var onIlluminanceChange: (illuminance: Float) -> Unit

    fun destroy() {
        sensorManager.unregisterListener(sensorListener)
    }

}
