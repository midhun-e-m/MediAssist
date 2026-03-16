package com.mediassist.app.ui.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.databinding.ActivityPlaceOrderBinding
import com.mediassist.app.network.RetrofitInstance
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PlaceOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceOrderBinding
    private var prescriptionUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var medicineName = ""
    private var price = ""
    private var pharmacistId = ""
    private var medicineImageUrl = ""

    private var requiresPrescription = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive Intent Data
        medicineName = intent.getStringExtra("medicineName") ?: ""
        price = intent.getStringExtra("price") ?: ""
        pharmacistId = intent.getStringExtra("pharmacistId") ?: ""
        medicineImageUrl = intent.getStringExtra("imageUrl") ?: ""
        requiresPrescription = intent.getBooleanExtra("requiresPrescription", false)

        // Set Medicine Info
        binding.tvMedicineName.text = medicineName
        binding.tvMedicinePrice.text = "₹$price"

        if (medicineImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(medicineImageUrl)
                .into(binding.imgMedicine)
        }

        // Hide upload button if prescription not required
        if (!requiresPrescription) {
            binding.btnUploadPrescription.visibility = android.view.View.GONE
            binding.imgPrescriptionPreview.visibility = android.view.View.GONE
        }

        // Upload prescription
        binding.btnUploadPrescription.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        // Place order
        binding.btnPlaceOrder.setOnClickListener {
            validateAndUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            prescriptionUri = data?.data

            binding.imgPrescriptionPreview.visibility = android.view.View.VISIBLE
            binding.imgPrescriptionPreview.setImageURI(prescriptionUri)
        }
    }

    private fun validateAndUpload() {

        val quantity = binding.etQuantity.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val pincode = binding.etPincode.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()

        if (quantity.isEmpty() || address.isEmpty() || pincode.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Fill all delivery details", Toast.LENGTH_SHORT).show()
            return
        }

        // Only check if medicine requires prescription
        if (requiresPrescription && prescriptionUri == null) {
            Toast.makeText(this, "Upload prescription", Toast.LENGTH_SHORT).show()
            return
        }

        // If prescription not required → create order directly
        if (!requiresPrescription) {
            createOrder(
                "",
                quantity,
                address,
                pincode,
                contact
            )
        } else {
            uploadPrescription(
                prescriptionUri!!,
                quantity,
                address,
                pincode,
                contact
            )
        }
    }

    private fun uploadPrescription(
        uri: Uri,
        quantity: String,
        address: String,
        pincode: String,
        contact: String
    ) {

        binding.btnPlaceOrder.isEnabled = false
        binding.btnPlaceOrder.text = "Uploading..."

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "prescription.jpg")
                file.outputStream().use { inputStream?.copyTo(it) }

                val requestFile =
                    file.asRequestBody("image/*".toMediaTypeOrNull())

                val body =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)

                val preset =
                    "mediassistProject".toRequestBody("text/plain".toMediaTypeOrNull())

                val response =
                    RetrofitInstance.api.uploadImage(body, preset)

                if (response.isSuccessful) {

                    val uploadedUrl =
                        response.body()?.get("secure_url").toString()

                    withContext(Dispatchers.Main) {
                        createOrder(
                            uploadedUrl,
                            quantity,
                            address,
                            pincode,
                            contact
                        )
                    }

                } else {
                    showError("Upload failed")
                }

            } catch (e: Exception) {
                showError("Upload error")
            }
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            binding.btnPlaceOrder.isEnabled = true
            binding.btnPlaceOrder.text = "Place Order"
            Toast.makeText(this@PlaceOrderActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOrder(
        prescriptionUrl: String,
        quantity: String,
        address: String,
        pincode: String,
        contact: String
    ) {

        val user = auth.currentUser ?: return

        val order = hashMapOf(
            "userId" to user.uid,
            "pharmacistId" to pharmacistId,
            "medicineName" to medicineName,
            "medicineImage" to medicineImageUrl,
            "price" to price,
            "quantity" to quantity,
            "address" to address,
            "pincode" to pincode,
            "contactNumber" to contact,
            "prescriptionUrl" to prescriptionUrl,
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Order placed successfully", Toast.LENGTH_SHORT).show()

                startActivity(
                    Intent(this, OrdersActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order failed", Toast.LENGTH_SHORT).show()
                binding.btnPlaceOrder.isEnabled = true
                binding.btnPlaceOrder.text = "Place Order"
            }
    }
}