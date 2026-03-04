package com.mediassist.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mediassist.app.R
import com.mediassist.app.sensors.MotionDetector
import com.mediassist.app.ui.user.EmergencyWarningActivity

class SensorMonitoringService : Service() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var motionDetector: MotionDetector

    override fun onCreate() {
        super.onCreate()

        startForegroundNotification()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        motionDetector = MotionDetector {

            val intent = Intent(this, EmergencyWarningActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }

        accelerometer?.let {
            sensorManager.registerListener(
                motionDetector,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(motionDetector)
        super.onDestroy()
    }

    private fun startForegroundNotification() {

        val channelId = "mediassist_sensor_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "MediAssist Crash Detection",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MediAssist Safety")
            .setContentText("Crash detection is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }
}