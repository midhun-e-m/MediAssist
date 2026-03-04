package com.mediassist.app.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt

class MotionDetector(private val onCrashDetected: () -> Unit) : SensorEventListener {

    private var lastTriggerTime = 0L

    override fun onSensorChanged(event: SensorEvent) {

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble())

        if (acceleration > SensorConstants.CRASH_ACCELERATION_THRESHOLD) {

            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTriggerTime > SensorConstants.CRASH_COOLDOWN_MS) {

                lastTriggerTime = currentTime
                onCrashDetected.invoke()

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}