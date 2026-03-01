package com.mediassist.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mediassist.app.R

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val btnUser = findViewById<Button>(R.id.btnUser)
        val btnDriver = findViewById<Button>(R.id.btnDriver)

        btnUser.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("ROLE", "USER")
            startActivity(intent)
        }

        btnDriver.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("ROLE", "DRIVER")
            startActivity(intent)
        }
    }
}
