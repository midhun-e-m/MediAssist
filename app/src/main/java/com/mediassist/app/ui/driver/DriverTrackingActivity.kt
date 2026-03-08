package com.mediassist.app.ui.driver

import android.animation.ValueAnimator
import org.osmdroid.util.GeoPoint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mediassist.app.R
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

class DriverTrackingActivity : AppCompatActivity() {


    private lateinit var map: MapView
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvDistance: TextView
    private lateinit var tvETA: TextView

    private lateinit var emergencyId: String

    private var driverMarker: Marker? = null
    private var patientMarker: Marker? = null
    private var routeLine: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().setUserAgentValue(packageName)

        setContentView(R.layout.activity_driver_tracking)

        emergencyId = intent.getStringExtra("emergencyId") ?: run {
            finish()
            return
        }

        map = findViewById(R.id.map)
        tvDistance = findViewById(R.id.tvDistance)
        tvETA = findViewById(R.id.tvETA)

        firestore = FirebaseFirestore.getInstance()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        map.controller.setZoom(16.0)

        listenLocations()
    }

    private var emergencyListener: ListenerRegistration? = null

    private fun listenLocations() {

        emergencyListener = firestore.collection("emergencies")
            .document(emergencyId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val driverLat = snapshot.getDouble("driverLat")
                val driverLng = snapshot.getDouble("driverLng")

                val patientLat = snapshot.getDouble("patientLat")
                val patientLng = snapshot.getDouble("patientLng")

                // Update patient marker
                if (patientLat != null && patientLng != null) {
                    updatePatient(patientLat, patientLng)
                }

                // Update driver marker
                if (driverLat != null && driverLng != null) {
                    updateDriver(driverLat, driverLng)
                }

                // Draw or update route when both positions are available
                if (driverLat != null && driverLng != null &&
                    patientLat != null && patientLng != null
                ) {
                    drawRoute(driverLat, driverLng, patientLat, patientLng)
                }
            }
    }

    private fun updateDriver(lat: Double, lng: Double) {

        val newPoint = GeoPoint(lat, lng)

        if (driverMarker == null) {

            driverMarker = Marker(map)
            driverMarker!!.title = "Ambulance"
            driverMarker!!.position = newPoint

            map.overlays.add(driverMarker)

            map.controller.setCenter(newPoint)
            map.controller.setZoom(17.0)

            return
        }

        val oldPoint = driverMarker!!.position

        animateMarker(driverMarker!!, oldPoint, newPoint)

        map.controller.animateTo(newPoint)
    }

    private fun updatePatient(lat: Double, lng: Double) {

        val newPoint = GeoPoint(lat, lng)

        if (patientMarker == null) {

            patientMarker = Marker(map)
            patientMarker!!.title = "Patient"
            patientMarker!!.position = newPoint

            map.overlays.add(patientMarker)
            return
        }

        val oldPoint = patientMarker!!.position

        animateMarker(patientMarker!!, oldPoint, newPoint)
    }


    private fun animateMarker(
        marker: Marker,
        start: GeoPoint,
        end: GeoPoint
    ) {

        val animator = ValueAnimator.ofFloat(0f, 1f)

        animator.duration = 1000

        animator.addUpdateListener { animation ->

            val fraction = animation.animatedValue as Float

            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction

            marker.position = GeoPoint(lat, lng)

            map.invalidate()
        }

        animator.start()
    }

    private fun drawRoute(
        driverLat: Double,
        driverLng: Double,
        patientLat: Double,
        patientLng: Double
    ) {
        if(routeLine != null) return
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

                val coordinates = route
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

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
    override fun onDestroy() {
        super.onDestroy()
        emergencyListener?.remove()
    }
}