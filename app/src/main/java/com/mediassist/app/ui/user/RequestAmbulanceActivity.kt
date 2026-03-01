package com.mediassist.app.ui.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.data.model.EmergencyContact
import com.mediassist.app.data.model.EmergencyRequest
import com.mediassist.app.data.repository.EmergencyRepository
import com.mediassist.app.services.SmsService

class RequestAmbulanceActivity : AppCompatActivity() {

    private val repo = EmergencyRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val GPS_TIMEOUT = 30000L
    private var isSmsSent = false
    private var isEmergencyCreated = false
    private var bestLocation: Location? = null

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_ambulance)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.btnRequestAmbulance).setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!hasPermissions()) {
                requestRequiredPermissions()
                return@setOnClickListener
            }

            if (!isLocationEnabled()) {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                return@setOnClickListener
            }

            repo.getContacts(uid) { contacts ->
                if (contacts.isEmpty()) {
                    Toast.makeText(this, "No emergency contacts found.", Toast.LENGTH_LONG).show()
                    return@getContacts
                }

                // Reset state
                isSmsSent = false
                isEmergencyCreated = false
                bestLocation = null

                Toast.makeText(this, "Locking GPS... Please wait.", Toast.LENGTH_SHORT).show()
                startLocationUpdates(contacts)
            }
        }
    }

    // --------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(contacts: List<EmergencyContact>) {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).setMinUpdateIntervalMillis(500)
            .setMinUpdateDistanceMeters(0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {

                    if (bestLocation == null || location.accuracy < bestLocation!!.accuracy) {
                        bestLocation = location
                        Toast.makeText(
                            applicationContext,
                            "Accuracy: ${location.accuracy.toInt()}m",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (location.accuracy <= 25) {
                        stopLocationUpdates()
                        finalizeEmergency(contacts, location)
                        return
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isSmsSent) {
                stopLocationUpdates()
                if (bestLocation != null) {
                    finalizeEmergency(contacts, bestLocation!!)
                } else {
                    fetchLastKnownAndFinalize(contacts)
                }
            }
        }, GPS_TIMEOUT)
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownAndFinalize(contacts: List<EmergencyContact>) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            finalizeEmergency(contacts, location)
        }
    }

    // --------------------------------------------------------------------
    // 🔥 THIS IS THE CRITICAL UNIFIED FUNCTION
    // --------------------------------------------------------------------

    private fun finalizeEmergency(
        contacts: List<EmergencyContact>,
        location: Location?
    ) {
        if (isSmsSent) return

        // 1️⃣ CREATE FIRESTORE EMERGENCY (ONCE)
        if (!isEmergencyCreated && location != null) {
            isEmergencyCreated = true

            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val emergency = EmergencyRequest(
                patientId = uid,
                patientLat = location.latitude,
                patientLng = location.longitude,
                status = "PENDING"
            )

            repo.createEmergency(emergency) {
                // Firestore emergency created → drivers will see it
            }
        }

        // 2️⃣ SEND SMS (EXISTING LOGIC)
        sendSmsToContacts(contacts, location)
    }

    // --------------------------------------------------------------------

    private fun sendSmsToContacts(
        contacts: List<EmergencyContact>,
        location: Location?
    ) {
        if (isSmsSent) return
        isSmsSent = true

        val message = buildEmergencyMessage(location)
        var successCount = 0

        contacts.forEach { contact ->
            try {
                SmsService.sendSms(contact.phone, message)
                successCount++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val status = if (successCount > 0)
            "🚨 Emergency sent"
        else
            "❌ SMS failed"

        Toast.makeText(this, status, Toast.LENGTH_LONG).show()
    }

    private fun buildEmergencyMessage(location: Location?): String {
        val sb = StringBuilder()
        sb.append("🚨 EMERGENCY 🚨\n")
        sb.append("MediAssist triggered.\n")

        if (location != null) {
            sb.append("\n📍 Location:\n")
            sb.append("https://maps.google.com/?q=${location.latitude},${location.longitude}")
            sb.append("\n(Accuracy: ${location.accuracy.toInt()}m)")
        } else {
            sb.append("\n📍 Location unavailable")
        }

        return sb.toString()
    }

    // --------------------------------------------------------------------

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun hasPermissions(): Boolean {
        val loc = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val sms = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        )
        return loc == PackageManager.PERMISSION_GRANTED &&
                sms == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRequiredPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
            ),
            PERMISSION_REQUEST_CODE
        )
    }
}
