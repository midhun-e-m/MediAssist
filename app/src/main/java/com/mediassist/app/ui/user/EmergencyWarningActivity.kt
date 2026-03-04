package com.mediassist.app.ui.user

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mediassist.app.R
import com.mediassist.app.services.EmergencyTriggerService

class EmergencyWarningActivity : AppCompatActivity() {

    private lateinit var countdownText: TextView
    private lateinit var cancelButton: Button

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_emergency_warning)

        countdownText = findViewById(R.id.countdownText)
        cancelButton = findViewById(R.id.cancelEmergency)

        startCountdown()

        cancelButton.setOnClickListener {
            timer?.cancel()
            finish()
        }
    }

    private fun startCountdown() {

        timer = object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val seconds = millisUntilFinished / 1000
                countdownText.text = "Sending emergency in $seconds"

            }

            override fun onFinish() {

                EmergencyTriggerService.triggerEmergency(this@EmergencyWarningActivity)
                finish()

            }

        }.start()

    }
}