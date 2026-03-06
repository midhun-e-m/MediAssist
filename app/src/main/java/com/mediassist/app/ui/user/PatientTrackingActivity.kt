package com.mediassist.app.ui.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.mediassist.app.R

class PatientTrackingActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var trackingRef: DatabaseReference

    private var driverMarker: Marker? = null
    private var patientMarker: Marker? = null

    private val requestId = "requestId123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(packageName)
        setContentView(R.layout.activity_live_tracking)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        trackingRef = FirebaseDatabase.getInstance()
            .getReference("liveTracking")
            .child(requestId)

        listenForLocation()
    }

    private fun listenForLocation() {

        trackingRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val driverLat = snapshot.child("driverLat").getValue(Double::class.java)
                val driverLng = snapshot.child("driverLng").getValue(Double::class.java)

                val patientLat = snapshot.child("patientLat").getValue(Double::class.java)
                val patientLng = snapshot.child("patientLng").getValue(Double::class.java)

                if (driverLat != null && driverLng != null) {
                    updateDriver(driverLat, driverLng)
                }

                if (patientLat != null && patientLng != null) {
                    updatePatient(patientLat, patientLng)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateDriver(lat: Double, lng: Double) {

        val point = GeoPoint(lat, lng)

        if (driverMarker == null) {
            driverMarker = Marker(map)
            driverMarker!!.title = "Ambulance"
            map.overlays.add(driverMarker)
        }

        driverMarker!!.position = point
        map.controller.setCenter(point)
        map.controller.setZoom(17.0)
        map.invalidate()
    }

    private fun updatePatient(lat: Double, lng: Double) {

        val point = GeoPoint(lat, lng)

        if (patientMarker == null) {
            patientMarker = Marker(map)
            patientMarker!!.title = "You"
            map.overlays.add(patientMarker)
        }

        patientMarker!!.position = point
        map.invalidate()
    }
}