package com.mediassist.app.network

data class NearbyResponse(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val geometry: Geometry
)

data class Geometry(
    val location: LatLngResult
)

data class LatLngResult(
    val lat: Double,
    val lng: Double
)
