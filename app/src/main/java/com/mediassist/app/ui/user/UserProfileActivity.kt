package com.mediassist.app.ui.user

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.data.model.User
import com.mediassist.app.viewmodel.UserViewModel

class UserProfileActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtAge = findViewById<EditText>(R.id.edtAge)

        // 🔽 CHANGED: AutoCompleteTextView for dropdown
        val edtGender = findViewById<AutoCompleteTextView>(R.id.edtGender)

        val edtBlood = findViewById<EditText>(R.id.edtBloodGroup)
        val edtConditions = findViewById<EditText>(R.id.edtConditions)
        val edtAllergies = findViewById<EditText>(R.id.edtAllergies)
        val edtHospital = findViewById<EditText>(R.id.edtHospital)
        val edtNotes = findViewById<EditText>(R.id.edtNotes)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // ✅ Gender dropdown setup
        val genderOptions = arrayOf("Male", "Female")
        val genderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genderOptions
        )
        edtGender.setAdapter(genderAdapter)

        // 🔄 Load profile
        viewModel.loadUserProfile(uid)

        viewModel.userProfile.observe(this) { user ->
            edtName.setText(user.name)
            edtAge.setText(user.age.toString())
            edtGender.setText(user.gender, false) // false = no filter
            edtBlood.setText(user.bloodGroup)
            edtConditions.setText(user.medicalConditions.joinToString(", "))
            edtAllergies.setText(user.allergies)
            edtHospital.setText(user.preferredHospital)
            edtNotes.setText(user.emergencyNotes)
        }

        // 💾 Save profile
        btnSave.setOnClickListener {
            val user = User(
                uid = uid,
                name = edtName.text.toString(),
                age = edtAge.text.toString().toIntOrNull() ?: 0,
                gender = edtGender.text.toString(),
                bloodGroup = edtBlood.text.toString(),
                medicalConditions = edtConditions.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() },
                allergies = edtAllergies.text.toString(),
                preferredHospital = edtHospital.text.toString(),
                emergencyNotes = edtNotes.text.toString()
            )

            viewModel.saveUserProfile(user)
        }

        viewModel.isSaved.observe(this) { success ->
            Toast.makeText(
                this,
                if (success) "Profile saved" else "Save failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
