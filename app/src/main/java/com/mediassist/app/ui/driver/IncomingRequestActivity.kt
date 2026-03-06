package com.mediassist.app.ui.driver

import android.content.Intent
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
    private val driverId = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var emergencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_request)

        emergencyId = intent.getStringExtra("emergencyId") ?: run {
            finish()
            return
        }

        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnReject = findViewById<Button>(R.id.btnReject)

        btnAccept.setOnClickListener {

            repo.acceptEmergency(
                emergencyId,
                driverId,
                onSuccess = {

                    // Start driver location tracking
                    val serviceIntent = Intent(this, TrackingLocationService::class.java)
                    serviceIntent.putExtra("emergencyId", emergencyId)
                    startService(serviceIntent)

                    // Open driver navigation screen
                    val intent = Intent(this, DriverTrackingActivity::class.java)
                    intent.putExtra("emergencyId", emergencyId)
                    startActivity(intent)

                    finish()
                },
                onFailure = {

                    Toast.makeText(
                        this,
                        "Request already accepted by another driver",
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