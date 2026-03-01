package com.mediassist.app.cart

data class CartItem(
    val medicineId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    var quantity: Int = 1,
    val imageUrl: String = "",
    val pharmacistId: String =""
)