package com.mediassist.app.ui.driver

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.data.repository.EmergencyRepository
import com.mediassist.app.services.TrackingLocationService

class IncomingRequestActivity : AppCompatActivity() {

    private val repo = EmergencyRepository()

    private val driverId by lazy {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    private var emergencyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_request)

        emergencyId = intent.getStringExtra("emergencyId")

        if (emergencyId == null) {
            Toast.makeText(this, "Emergency ID missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnReject = findViewById<Button>(R.id.btnReject)

        btnAccept.setOnClickListener {

            val driver = driverId
            val id = emergencyId

            if (driver == null || id == null) {
                Toast.makeText(this, "Driver authentication error", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            repo.acceptEmergency(
                id,
                driver,
                onSuccess = {

                    // 🚑 Start foreground location tracking
                    val serviceIntent = Intent(this, TrackingLocationService::class.java)
                    serviceIntent.putExtra("emergencyId", id)
                    serviceIntent.putExtra("userType", "DRIVER")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }

                    // 🗺 Open tracking screen
                    val intent = Intent(this, DriverTrackingActivity::class.java)
                    intent.putExtra("emergencyId", id)
                    startActivity(intent)

                    finish()
                },
                onFailure = {

                    Toast.makeText(
                        this,
                        "Request already accepted",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
            )
        }

        btnReject.setOnClickListener {
            finish()
        }
    }
}