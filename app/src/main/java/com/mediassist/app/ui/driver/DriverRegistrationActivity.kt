package com.mediassist.app.ui.driver

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mediassist.app.R
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue


class DriverRegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        val edtName = findViewById<EditText>(R.id.edtDriverName)
        val edtPhone = findViewById<EditText>(R.id.edtDriverPhone)
        val edtLicense = findViewById<EditText>(R.id.edtLicenseNumber)
        val edtAmbulanceNo = findViewById<EditText>(R.id.edtAmbulanceNumber)
        val spinnerType = findViewById<Spinner>(R.id.spinnerAmbulanceType)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitDriver)

        // ✅ Spinner adapter AFTER spinnerType is defined
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.ambulance_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnSubmit.setOnClickListener {

            if (
                edtName.text.isNullOrEmpty() ||
                edtPhone.text.isNullOrEmpty() ||
                edtLicense.text.isNullOrEmpty() ||
                edtAmbulanceNo.text.isNullOrEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val driverDoc = db.collection("drivers").document(currentUser.uid)

            val driverData = hashMapOf(
                "uid" to currentUser.uid,
                "name" to edtName.text.toString(),
                "email" to currentUser.email,
                "phone" to edtPhone.text.toString(),
                "licenseNumber" to edtLicense.text.toString(),
                "ambulanceNumber" to edtAmbulanceNo.text.toString(),
                "ambulanceType" to spinnerType.selectedItem.toString(),
                "approvalStatus" to "PENDING",
                "availability" to "OFFLINE",
                "createdAt" to FieldValue.serverTimestamp()
            )

            driverDoc.set(driverData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Registration submitted. Awaiting admin approval.",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(
                        Intent(this, PendingApprovalActivity::class.java)
                    )
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
        }

    }

}
