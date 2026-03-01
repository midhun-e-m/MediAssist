package com.mediassist.app.ui.user

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.R

class OrdersActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView

    // Prevent duplicate notifications
    private val notifiedOrders = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        recyclerView = findViewById(R.id.rvOrders)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNavigation()
        loadOrdersRealtime()
    }

    private fun loadOrdersRealtime() {

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("orders")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { value, error ->

                if (error != null || value == null) return@addSnapshotListener

                val orders = value.documents.mapNotNull { doc ->
                    val data = doc.data
                    data?.toMutableMap()?.apply {
                        put("orderId", doc.id) // Needed for payment update
                    }
                }

                recyclerView.adapter = OrdersAdapter(orders)

                // 🔔 Check for ACCEPTED status
                orders.forEach { order ->
                    val orderId = order["orderId"]?.toString() ?: return@forEach
                    val status = order["status"]?.toString()

                    if (status == "ACCEPTED" && !notifiedOrders.contains(orderId)) {
                        showAcceptedNotification()
                        notifiedOrders.add(orderId)
                    }
                }
            }
    }

    private fun showAcceptedNotification() {

        val channelId = "order_channel"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Order Accepted")
            .setContentText("Your prescription has been approved. Please complete payment.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun setupBottomNavigation() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_orders

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_medicines -> {
                    startActivity(Intent(this, BrowseMedicinesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_orders -> true

                else -> false
            }
        }
    }
}