package com.mediassist.app.ui.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.databinding.ActivityOrderRouteTrackingBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderRouteTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderRouteTrackingBinding
    private val db = FirebaseFirestore.getInstance()

    private val trackingList = mutableListOf<TrackingItem>()
    private lateinit var adapter: TrackingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderRouteTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TrackingAdapter(trackingList)

        binding.recyclerTracking.layoutManager = LinearLayoutManager(this)
        binding.recyclerTracking.adapter = adapter

        val orderId = intent.getStringExtra("orderId") ?: return

        loadTracking(orderId)
    }

    private fun loadTracking(orderId: String) {

        db.collection("orderTrackingHistory")
            .whereEqualTo("orderId", orderId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                trackingList.clear()

                for (doc in snapshot.documents) {

                    val location = doc.getString("location") ?: "Unknown"
                    val status = doc.getString("status") ?: ""

                    val timestamp = doc.getTimestamp("timestamp")?.toDate()

                    val formattedTime = timestamp?.let {
                        SimpleDateFormat(
                            "dd MMM yyyy • hh:mm a",
                            Locale.getDefault()
                        ).format(it)
                    } ?: ""

                    trackingList.add(
                        TrackingItem(
                            location,
                            status.replace("_"," "),
                            formattedTime
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
    }
}