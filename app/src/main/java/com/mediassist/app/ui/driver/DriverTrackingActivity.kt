package com.mediassist.app.ui.driver

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.R
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

class DriverTrackingActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvDistance: TextView
    private lateinit var tvETA: TextView

    private var driverMarker: Marker? = null
    private var patientMarker: Marker? = null
    private var routeLine: Polyline? = null

    private val requestId = "FtLrDIp7CDigMDuLJi5X"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(packageName)

        setContentView(R.layout.activity_driver_tracking)

        map = findViewById(R.id.map)
        tvDistance = findViewById(R.id.tvDistance)
        tvETA = findViewById(R.id.tvETA)

        firestore = FirebaseFirestore.getInstance()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        map.controller.setZoom(16.0)
        map.controller.setCenter(GeoPoint(9.8743, 76.9734))

        listenLocations()
    }

    private fun listenLocations() {

        firestore.collection("emergencies")
            .document(requestId)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null && snapshot.exists()) {

                    val driverLat = snapshot.getDouble("driverLat")
                    val driverLng = snapshot.getDouble("driverLng")

                    val patientLat = snapshot.getDouble("patientLat")
                    val patientLng = snapshot.getDouble("patientLng")

                    if (driverLat != null && driverLng != null)
                        updateDriver(driverLat, driverLng)

                    if (patientLat != null && patientLng != null)
                        updatePatient(patientLat, patientLng)

                    if (driverLat != null && driverLng != null &&
                        patientLat != null && patientLng != null) {

                        drawRoute(driverLat, driverLng, patientLat, patientLng)
                    }
                }
            }
    }

    private fun updateDriver(lat: Double, lng: Double) {

        val point = GeoPoint(lat, lng)

        if (driverMarker == null) {
            driverMarker = Marker(map)
            driverMarker!!.title = "Ambulance"
            map.overlays.add(driverMarker)
        }

        driverMarker!!.position = point

        map.controller.animateTo(point)
        map.controller.setZoom(17.0)

        map.invalidate()
    }

    private fun updatePatient(lat: Double, lng: Double) {

        val point = GeoPoint(lat, lng)

        if (patientMarker == null) {
            patientMarker = Marker(map)
            patientMarker!!.title = "Patient"
            map.overlays.add(patientMarker)
        }

        patientMarker!!.position = point
    }

    private fun drawRoute(
        driverLat: Double,
        driverLng: Double,
        patientLat: Double,
        patientLng: Double
    ) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "$driverLng,$driverLat;$patientLng,$patientLat" +
                    "?overview=full&geometries=geojson"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val json = JSONObject(response.body!!.string())

                val route = json.getJSONArray("routes").getJSONObject(0)

                val distance = route.getDouble("distance") / 1000
                val duration = route.getDouble("duration") / 60

                val geometry = route.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")

                val geoPoints = ArrayList<GeoPoint>()

                for (i in 0 until coordinates.length()) {

                    val coord = coordinates.getJSONArray(i)

                    val lng = coord.getDouble(0)
                    val lat = coord.getDouble(1)

                    geoPoints.add(GeoPoint(lat, lng))
                }

                runOnUiThread {

                    if (routeLine != null)
                        map.overlays.remove(routeLine)

                    routeLine = Polyline()
                    routeLine!!.setPoints(geoPoints)
                    routeLine!!.outlinePaint.strokeWidth = 10f

                    map.overlays.add(routeLine)

                    tvDistance.text = "Distance: %.2f km".format(distance)
                    tvETA.text = "ETA: %.0f min".format(duration)

                    map.invalidate()
                }
            }
        })
    }
}