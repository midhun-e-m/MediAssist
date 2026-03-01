package com.mediassist.app.ui.user

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.R
import com.mediassist.app.cart.CartManager

class CheckoutActivity : AppCompatActivity() {

    private lateinit var addressEditText: EditText
    private lateinit var paymentGroup: RadioGroup
    private lateinit var placeOrderBtn: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var splitOrders: ArrayList<HashMap<String, Any>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        addressEditText = findViewById(R.id.addressEditText)
        paymentGroup = findViewById(R.id.paymentRadioGroup)
        placeOrderBtn = findViewById(R.id.placeOrderBtn)

        // 🔥 Receive split orders from CartActivity
        splitOrders =
            intent.getSerializableExtra("splitOrders")
                    as? ArrayList<HashMap<String, Any>> ?: arrayListOf()

        if (splitOrders.isEmpty()) {
            Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        placeOrderBtn.setOnClickListener {

            val address = addressEditText.text.toString().trim()

            if (address.isEmpty()) {
                Toast.makeText(this, "Enter address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPaymentId = paymentGroup.checkedRadioButtonId
            if (selectedPaymentId == -1) {
                Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod =
                findViewById<RadioButton>(selectedPaymentId).text.toString()

            createOrders(address, paymentMethod)
        }
    }

    private fun createOrders(address: String, paymentMethod: String) {

        val userId = auth.currentUser?.uid ?: return

        var successCount = 0
        val totalOrders = splitOrders.size

        for (order in splitOrders) {

            order["address"] = address
            order["paymentMethod"] = paymentMethod
            order["paymentStatus"] =
                if (paymentMethod.contains("COD", true)) "NOT_PAID"
                else "PENDING"

            order["status"] = "PENDING"
            order["timestamp"] = System.currentTimeMillis()
            order["userId"] = userId

            db.collection("orders")
                .add(order)
                .addOnSuccessListener {

                    successCount++

                    if (successCount == totalOrders) {
                        Toast.makeText(
                            this,
                            "Orders Placed Successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        CartManager.clearCart()
                        finish()
                    }
                }
        }
    }
}