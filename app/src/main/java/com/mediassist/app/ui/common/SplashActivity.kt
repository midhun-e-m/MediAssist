package com.mediassist.app.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.R
import com.mediassist.app.ui.auth.RoleSelectionActivity
import com.mediassist.app.ui.user.UserDashboardActivity
import com.mediassist.app.ui.driver.DriverDashboardActivity
import com.mediassist.app.ui.driver.DriverRegistrationActivity
import com.mediassist.app.ui.driver.PendingApprovalActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Safety timeout (3 seconds max splash)
        window.decorView.postDelayed({
            if (!isFinishing) {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
                finish()
            }
        }, 3000)

        if (currentUser == null) {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser.uid)

        userRef.get()
            .addOnSuccessListener { userDoc ->

                if (!userDoc.exists()) {
                    auth.signOut()
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                val role = userDoc.getString("role")

                if (role == "DRIVER") {
                    handleDriverFlow(db, currentUser.uid)
                } else {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
                finish()
            }
    }

    private fun handleDriverFlow(db: FirebaseFirestore, uid: String) {

        db.collection("drivers").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->

                val approvalStatus = userDoc.getString("approvalStatus")

                when (approvalStatus) {

                    "PENDING" -> {
                        startActivity(
                            Intent(this, PendingApprovalActivity::class.java)
                        )
                    }

                    "APPROVED" -> {
                        startActivity(
                            Intent(this, DriverDashboardActivity::class.java)
                        )
                    }

                    else -> {
                        // If approvalStatus missing → registration incomplete
                        startActivity(
                            Intent(this, DriverRegistrationActivity::class.java)
                        )
                    }
                }

                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
                finish()
            }
    }
}
