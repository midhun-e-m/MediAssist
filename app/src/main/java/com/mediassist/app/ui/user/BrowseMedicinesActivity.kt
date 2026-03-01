package com.mediassist.app.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mediassist.app.R
import com.mediassist.app.databinding.ActivityBrowseMedicinesBinding

class BrowseMedicinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseMedicinesBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseMedicinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.rvMedicines.layoutManager = GridLayoutManager(this, 2)

        // Setup Bottom Navigation
        setupBottomNavigation()

        loadMedicines()
    }

    private fun setupBottomNavigation() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_medicines

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_medicines -> true

                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, OrdersActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }

                else -> false
            }
        }
    }

    private fun loadMedicines() {
        db.collection("medicines")
            .get()
            .addOnSuccessListener { result ->

                val medicines = result.documents.mapNotNull { doc ->
                    doc.data?.apply {
                        put("id", doc.id)
                    }
                }

                val adapter = MedicineAdapter(medicines) { medicine ->

                    val intent = Intent(this, PlaceOrderActivity::class.java)
                    intent.putExtra("medicineName", medicine["name"]?.toString() ?: "")
                    intent.putExtra("price", medicine["price"]?.toString() ?: "")
                    intent.putExtra("pharmacistId", medicine["pharmacistId"]?.toString() ?: "")
                    intent.putExtra("imageUrl", medicine["imageUrl"]?.toString() ?: "")
                    startActivity(intent)
                }

                binding.rvMedicines.adapter = adapter
            }
    }
}