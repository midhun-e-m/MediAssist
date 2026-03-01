package com.mediassist.app.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.mediassist.app.R
import com.mediassist.app.ui.auth.LoginActivity

class DriverDashboardActivity : AppCompatActivity() {

    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var googleSignInClient: GoogleSignInClient

    // Emergency UI
    private lateinit var emergencyCard: View
    private lateinit var tvLocation: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnReject: Button
    private lateinit var switchAvailability: SwitchMaterial
    private lateinit var tvStatus: TextView

    private val driverId by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private val db = FirebaseFirestore.getInstance()

    private var activeEmergencyId: String? = null
    private var emergencyListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        // ================= GOOGLE SIGN-IN CLIENT =================
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ================= DRAWER SETUP =================
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_logout -> {
                    logout()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // ================= DRIVER UI =================
        switchAvailability = findViewById(R.id.switchAvailability)
        tvStatus = findViewById(R.id.tvStatus)
        emergencyCard = findViewById(R.id.emergencyCard)
        tvLocation = findViewById(R.id.tvLocation)
        btnAccept = findViewById(R.id.btnAccept)
        btnReject = findViewById(R.id.btnReject)

        tvStatus.text = "OFFLINE"
        emergencyCard.visibility = View.GONE

        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) goOnline() else goOffline()
        }
    }

    // ================= PROPER LOGOUT =================
    private fun logout() {

        emergencyListener?.remove()

        FirebaseAuth.getInstance().signOut()

        googleSignInClient.revokeAccess().addOnCompleteListener {
            redirectToLogin()
        }
    }



    // ================= GO ONLINE =================
    private fun goOnline() {

        if (driverId.isEmpty()) {
            redirectToLogin()
            return
        }

        val driverRef = db.collection("drivers").document(driverId)

        driverRef.get().addOnSuccessListener { snapshot ->

            val approvalStatus = snapshot.getString("approvalStatus")

            if (approvalStatus != "APPROVED") {
                Toast.makeText(
                    this,
                    "Driver not approved yet",
                    Toast.LENGTH_LONG
                ).show()
                switchAvailability.isChecked = false
                return@addOnSuccessListener
            }

            tvStatus.text = "ONLINE"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_light))

            driverRef.set(
                mapOf("availability" to "ONLINE"),
                SetOptions.merge()
            )

            listenForEmergencies()
        }
    }

    // ================= GO OFFLINE =================
    private fun goOffline() {

        tvStatus.text = "OFFLINE"
        tvStatus.setTextColor(getColor(android.R.color.holo_red_light))

        emergencyCard.visibility = View.GONE
        activeEmergencyId = null

        emergencyListener?.remove()

        db.collection("drivers")
            .document(driverId)
            .set(
                mapOf("availability" to "OFFLINE"),
                SetOptions.merge()
            )
    }

    // ================= LISTEN FOR EMERGENCIES =================
    private fun listenForEmergencies() {

        emergencyListener = db.collection("emergencies")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshots, _ ->

                if (snapshots == null || snapshots.isEmpty) {
                    emergencyCard.visibility = View.GONE
                    return@addSnapshotListener
                }

                val doc = snapshots.documents.first()
                val emergencyId = doc.id

                if (emergencyId == activeEmergencyId) return@addSnapshotListener

                activeEmergencyId = emergencyId

                val lat = doc.getDouble("patientLat")
                val lng = doc.getDouble("patientLng")

                tvLocation.text = "Patient Location:\n$lat , $lng"

                emergencyCard.visibility = View.VISIBLE

                btnAccept.setOnClickListener {
                    acceptEmergency(emergencyId)
                }

                btnReject.setOnClickListener {
                    emergencyCard.visibility = View.GONE
                    activeEmergencyId = null
                }
            }
    }

    // ================= ACCEPT EMERGENCY =================
    private fun acceptEmergency(emergencyId: String) {

        val driverRef = db.collection("drivers").document(driverId)
        val emergencyRef = db.collection("emergencies").document(emergencyId)

        emergencyRef.get().addOnSuccessListener { doc ->

            val patientId = doc.getString("patientId")
            val patientLat = doc.getDouble("patientLat")
            val patientLng = doc.getDouble("patientLng")

            // 1️⃣ Update Firestore status
            emergencyRef.update(
                mapOf(
                    "status" to "ACCEPTED",
                    "assignedDriverId" to driverId
                )
            )

            // 2️⃣ Update driver availability
            driverRef.update("availability", "BUSY")

            // 3️⃣ Create Realtime tracking node
            if (patientId != null && patientLat != null && patientLng != null) {

                val trackingRef = com.google.firebase.database.FirebaseDatabase
                    .getInstance()
                    .getReference("liveTracking")
                    .child(emergencyId)

                val trackingData = mapOf(
                    "patientId" to patientId,
                    "driverId" to driverId,
                    "patientLat" to patientLat,
                    "patientLng" to patientLng
                )

                trackingRef.setValue(trackingData)
            }

            tvStatus.text = "BUSY"
            tvStatus.setTextColor(getColor(android.R.color.holo_orange_light))

            Toast.makeText(this, "Emergency Accepted", Toast.LENGTH_SHORT).show()

            emergencyCard.visibility = View.GONE

            // 4️⃣ Open tracking screen
            val intent = Intent(this, DriverTrackingActivity::class.java)
            intent.putExtra("requestId", emergencyId)
            startActivity(intent)
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        emergencyListener?.remove()
    }
}
