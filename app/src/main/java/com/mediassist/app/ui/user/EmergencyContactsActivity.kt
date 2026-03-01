package com.mediassist.app.ui.user

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.mediassist.app.R
import com.mediassist.app.data.model.EmergencyContact
import com.mediassist.app.data.repository.EmergencyRepository

class EmergencyContactsActivity : AppCompatActivity() {

    private val repo = EmergencyRepository()
    private val uid by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val displayList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        listView = findViewById(R.id.listContacts)
        val btnAdd = findViewById<Button>(R.id.btnAddContact)
        val btnSave = findViewById<Button>(R.id.btnSaveContacts)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            101
        )

        btnAdd.setOnClickListener {
            pickContactLauncher.launch(null)
        }

        btnSave.setOnClickListener {
            finish()
        }

        loadContacts()
    }

    private val pickContactLauncher =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            uri?.let { handlePickedContact(it) }
        }

    private fun handlePickedContact(uri: Uri) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val id =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                fetchPhoneAndSave(id, name)
            }
        }
    }

    private fun fetchPhoneAndSave(contactId: String, name: String) {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use {
            if (it.moveToFirst()) {
                val phone =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                repo.addContact(uid, EmergencyContact(name, phone)) {
                    loadContacts()
                }
            }
        }
    }

    private fun loadContacts() {
        repo.getContacts(uid) { contacts ->
            displayList.clear()
            contacts.forEach {
                displayList.add("${it.name} • ${it.phone}")
            }
            adapter.notifyDataSetChanged()
        }
    }
}
