package com.mediassist.app.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TrackingLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var requestId: String? = null
    private var userType: String? = null // "DRIVER" or "PATIENT"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        requestId = intent?.getStringExtra("requestId")
        userType = intent?.getStringExtra("userType")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        startLocationUpdates()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null && requestId != null) {
                    updateLocation(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun updateLocation(location: Location) {

        val trackingRef = FirebaseDatabase.getInstance()
            .getReference("liveTracking")
            .child(requestId!!)

        val updates = if (userType == "DRIVER") {
            mapOf(
                "driverLat" to location.latitude,
                "driverLng" to location.longitude
            )
        } else {
            mapOf(
                "patientLat" to location.latitude,
                "patientLng" to location.longitude
            )
        }

        trackingRef.updateChildren(updates)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}