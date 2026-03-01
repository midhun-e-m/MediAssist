package com.mediassist.app.ui.driver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.data.repository.EmergencyRepository
//import com.mediassist.app.ui.user.LiveTrackingActivity

class IncomingRequestActivity : AppCompatActivity() {

    private val repo = EmergencyRepository()
    private val driverId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var emergencyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_request)

        emergencyId = intent.getStringExtra("emergencyId") ?: run {
            finish(); return
        }

        findViewById<Button>(R.id.btnAccept).setOnClickListener {
            repo.acceptEmergency(
                emergencyId,
                driverId,
                onSuccess = {
                    //startActivity(
                      //  Intent(this, LiveTrackingActivity::class.java)
                        //    .putExtra("emergencyId", emergencyId)
                    //)
                    finish()
                },
                onFailure = {
                    Toast.makeText(
                        this,
                        "Already accepted by another driver",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            )
        }

        findViewById<Button>(R.id.btnReject).setOnClickListener {
            finish()
        }
    }
}
