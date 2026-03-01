package com.mediassist.app.data.model

data class MedicineOrder(
    val orderId: String = "",
    val userId: String = "",
    val pharmacistId: String = "",
    val medicineList: List<Map<String, Any>> = emptyList(),
    val prescriptionUrl: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "PENDING",
    val timestamp: Any? = null,
    val deliveryPartnerId: String = ""
)