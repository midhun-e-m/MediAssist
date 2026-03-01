package com.mediassist.app.cart

object CartManager {

    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(item: CartItem) {
        val existingItem = cartItems.find { it.medicineId == item.medicineId }

        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            cartItems.add(item)
        }
    }

    fun removeFromCart(medicineId: String) {
        cartItems.removeAll { it.medicineId == medicineId }
    }

    fun getCartItems(): List<CartItem> {
        return cartItems
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getTotalAmount(): Double {
        return cartItems.sumOf { it.price * it.quantity }
    }

    fun isCartEmpty(): Boolean {
        return cartItems.isEmpty()
    }
}