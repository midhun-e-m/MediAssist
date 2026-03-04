package com.mediassist.app.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.ui.auth.LoginActivity
import com.mediassist.app.viewmodel.UserViewModel
import com.mediassist.app.services.SensorMonitoringService


class UserDashboardActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.startForegroundService(
            this,
            Intent(this, SensorMonitoringService::class.java)
        )

        setContentView(R.layout.activity_user_dashboard)

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

        // ================= DASHBOARD UI =================
        val txtProfileStatus = findViewById<TextView>(R.id.txtProfileStatus)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnContacts = findViewById<Button>(R.id.btnContacts)
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        val cardMedicalAssistant = findViewById<Button>(R.id.cardMedicalAssistant)
        val btnBrowseMedicines = findViewById<Button>(R.id.btnBrowseMedicines) // 👈 NEW BUTTON

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            redirectToLogin()
            return
        }

        // ================= LOAD PROFILE =================
        viewModel.loadUserProfile(uid)

        viewModel.userProfile.observe(this) { user ->
            if (user.isProfileComplete()) {
                txtProfileStatus.text = "Profile completed"
                txtProfileStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.success_green)
                )
            } else {
                txtProfileStatus.text = "Tap to complete your medical profile"
                txtProfileStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.warning_orange)
                )
            }
        }

        // ================= BUTTON ACTIONS =================
        btnProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        btnContacts.setOnClickListener {
            startActivity(Intent(this, EmergencyContactsActivity::class.java))
        }

        cardMedicalAssistant.setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java))
        }

        btnEmergency.setOnClickListener {
            startActivity(Intent(this, RequestAmbulanceActivity::class.java))
        }

        // 🔥 NEW: Browse Medicines
        btnBrowseMedicines.setOnClickListener {
            startActivity(Intent(this, BrowseMedicinesActivity::class.java))
        }

        // ================= DRAWER MENU ACTIONS =================
        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                }

                R.id.nav_contacts -> {
                    startActivity(Intent(this, EmergencyContactsActivity::class.java))
                }

                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatBotActivity::class.java))
                }

                R.id.nav_nearby -> {
                    startActivity(Intent(this, NearbyHospitalsActivity::class.java))
                }

                R.id.nav_logout -> {
                    logout()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // ================= LOGOUT =================
    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        googleSignInClient.revokeAccess().addOnCompleteListener {
            redirectToLogin()
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
}