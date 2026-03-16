package com.mediassist.app.ui.user

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
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

class NearbyHospitalsActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hospitalAdapter: HospitalAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val hospitalList = mutableListOf<Hospital>()
    private val client = OkHttpClient()

    private lateinit var userLocation: GeoPoint

    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osm", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_nearby_hospitals)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)

        recyclerView = findViewById(R.id.hospitalRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
    }

    // ================= PERMISSION =================

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // ================= LOCATION =================

    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {
                updateMap(location.latitude, location.longitude)
            } else {
                requestSingleUpdate()
            }
        }
    }

    private fun requestSingleUpdate() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000
        ).setMaxUpdates(1).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateMap(location.latitude, location.longitude)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            mainLooper
        )
    }

    private fun updateMap(lat: Double, lng: Double) {

        map.overlays.clear()

        userLocation = GeoPoint(lat, lng)

        map.controller.setCenter(userLocation)

        addUserMarker(userLocation)

        fetchNearbyHospitals(lat, lng)
    }

    // ================= USER MARKER =================

    private fun addUserMarker(point: GeoPoint) {

        val marker = Marker(map)
        marker.position = point
        marker.title = "You are here"

        marker.icon = ContextCompat.getDrawable(
            this,
            org.osmdroid.library.R.drawable.ic_menu_mylocation
        )

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        map.overlays.add(marker)
    }

    // ================= FETCH HOSPITALS =================

    private fun fetchNearbyHospitals(userLat: Double, userLng: Double) {

        val query = """
            [out:json];
            (
              node["amenity"="hospital"](around:5000,$userLat,$userLng);
            );
            out;
        """.trimIndent()

        val requestBody = FormBody.Builder()
            .add("data", query)
            .build()

        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    parseHospitals(body, userLat, userLng)
                }
            }
        })
    }

    private fun parseHospitals(jsonData: String, userLat: Double, userLng: Double) {

        val json = JSONObject(jsonData)
        val elements = json.getJSONArray("elements")

        hospitalList.clear()

        for (i in 0 until elements.length()) {

            val obj = elements.getJSONObject(i)

            val lat = obj.getDouble("lat")
            val lon = obj.getDouble("lon")

            val tags = obj.optJSONObject("tags")
            val name = tags?.optString("name", "Hospital") ?: "Hospital"

            fetchRoadDistance(userLat, userLng, lat, lon, name)
        }
    }

    // ================= DISTANCE =================

    private fun fetchRoadDistance(
        userLat: Double,
        userLng: Double,
        hospitalLat: Double,
        hospitalLng: Double,
        hospitalName: String
    ) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "$userLng,$userLat;$hospitalLng,$hospitalLat?overview=false"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: return
                val json = JSONObject(body)

                val route = json.getJSONArray("routes").getJSONObject(0)

                val distanceMeters = route.getDouble("distance")
                val durationSeconds = route.getDouble("duration")

                val distanceKm = distanceMeters / 1000
                val durationMinutes = durationSeconds / 60

                runOnUiThread {

                    val hospitalPoint = GeoPoint(hospitalLat, hospitalLng)

                    hospitalList.add(
                        Hospital(
                            hospitalName,
                            hospitalLat,
                            hospitalLng,
                            distanceKm
                        )
                    )

                    hospitalList.sortBy { it.distance }

                    hospitalAdapter = HospitalAdapter(hospitalList)
                    recyclerView.adapter = hospitalAdapter

                    val marker = Marker(map)
                    marker.position = hospitalPoint

                    marker.title =
                        "$hospitalName\n%.2f km | %.0f mins".format(distanceKm, durationMinutes)

                    marker.icon = ContextCompat.getDrawable(
                        this@NearbyHospitalsActivity,
                        org.osmdroid.library.R.drawable.ic_menu_compass
                    )

                    marker.setOnMarkerClickListener { _, _ ->
                        drawRoute(userLocation, hospitalPoint)
                        true
                    }

                    map.overlays.add(marker)

                    map.invalidate()
                }
            }
        })
    }

    // ================= DRAW ROUTE =================

    private fun drawRoute(start: GeoPoint, end: GeoPoint) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string() ?: return

                val json = JSONObject(body)
                val routes = json.getJSONArray("routes")

                if (routes.length() == 0) return

                val geometry =
                    routes.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                val routePoints = mutableListOf<GeoPoint>()

                for (i in 0 until geometry.length()) {

                    val coord = geometry.getJSONArray(i)

                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)

                    routePoints.add(GeoPoint(lat, lon))
                }

                runOnUiThread {

                    val polyline = Polyline()

                    polyline.setPoints(routePoints)
                    polyline.outlinePaint.color = Color.BLUE
                    polyline.outlinePaint.strokeWidth = 10f

                    map.overlays.add(polyline)

                    map.invalidate()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}