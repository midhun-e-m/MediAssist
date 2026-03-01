package com.mediassist.app.ui.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.R
import com.mediassist.app.cart.CartManager
import com.mediassist.app.network.RetrofitInstance
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CartActivity : AppCompatActivity() {

    private lateinit var totalText: TextView
    private lateinit var checkoutBtn: Button
    private lateinit var bottomNav: BottomNavigationView

    private var prescriptionUri: Uri? = null
    private val PICK_IMAGE = 100

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        totalText = findViewById(R.id.totalAmountText)
        checkoutBtn = findViewById(R.id.checkoutBtn)
        bottomNav = findViewById(R.id.bottomNavigation)

        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.cartRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = CartAdapter(CartManager.getCartItems().toMutableList()) {
            updateTotal()
        }

        recyclerView.adapter = adapter

        updateTotal()
        setupBottomNavigation()

        checkoutBtn.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openImagePicker()
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_cart
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_medicines -> {
                    startActivity(Intent(this, BrowseMedicinesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_cart -> true
                R.id.nav_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateTotal() {
        totalText.text = "Total: ₹${CartManager.getTotalAmount()}"
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            prescriptionUri = data?.data
            if (prescriptionUri != null) {
                uploadPrescription(prescriptionUri!!)
            }
        }
    }

    private fun uploadPrescription(uri: Uri) {

        checkoutBtn.isEnabled = false
        checkoutBtn.text = "Uploading..."

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "prescription.jpg")
                file.outputStream().use { inputStream?.copyTo(it) }

                val requestFile =
                    file.asRequestBody("image/*".toMediaTypeOrNull())

                val body =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)

                val preset =
                    "mediassistProject".toRequestBody("text/plain".toMediaTypeOrNull())

                val response =
                    RetrofitInstance.api.uploadImage(body, preset)

                if (response.isSuccessful) {

                    val uploadedUrl =
                        response.body()?.get("secure_url").toString()

                    withContext(Dispatchers.Main) {
                        createSplitOrders(uploadedUrl)
                    }

                } else {
                    showError("Upload failed")
                }

            } catch (e: Exception) {
                showError("Upload error")
            }
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            checkoutBtn.isEnabled = true
            checkoutBtn.text = "Proceed to Checkout"
            Toast.makeText(this@CartActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createSplitOrders(prescriptionUrl: String) {

        val user = auth.currentUser ?: return
        val cartItems = CartManager.getCartItems()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val grouped = cartItems.groupBy { it.pharmacistId }

        val splitOrders = ArrayList<HashMap<String, Any>>()

        for ((pharmacistId, items) in grouped) {

            val itemsList = items.map {
                hashMapOf(
                    "medicineId" to it.medicineId,
                    "name" to it.medicineName,
                    "price" to it.price,
                    "quantity" to it.quantity,
                    "imageUrl" to it.imageUrl
                )
            }

            val totalAmount = items.sumOf { it.price * it.quantity }

            val orderData = hashMapOf(
                "userId" to user.uid,
                "pharmacistId" to pharmacistId,
                "items" to itemsList,
                "totalAmount" to totalAmount,
                "prescriptionUrl" to prescriptionUrl,
                "status" to "PENDING",
                "paymentStatus" to "NOT_PAID",
                "timestamp" to System.currentTimeMillis()
            )

            splitOrders.add(orderData)
        }

        // 🔥 Navigate to CheckoutActivity instead of saving now
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra("splitOrders", splitOrders)
        startActivity(intent)
    }
}