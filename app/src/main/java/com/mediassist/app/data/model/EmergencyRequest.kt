package com.mediassist.app.data.model

data class EmergencyRequest(
    val id: String = "",
    val patientId: String = "",
    val patientLat: Double = 0.0,
    val patientLng: Double = 0.0,
    val status: String = "pending", // pending | accepted | completed
    val assignedDriverId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
