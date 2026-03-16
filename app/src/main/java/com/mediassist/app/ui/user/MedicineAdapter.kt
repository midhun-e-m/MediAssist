package com.mediassist.app.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mediassist.app.cart.CartItem
import com.mediassist.app.cart.CartManager
import com.mediassist.app.databinding.ItemMedicineBinding

class MedicineAdapter(
    private val medicines: List<Map<String, Any>>,
    private val onBuyClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(val binding: ItemMedicineBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {

        val context = holder.itemView.context
        val medicine = medicines[position]

        val name = medicine["name"]?.toString() ?: ""
        val priceString = medicine["price"]?.toString() ?: "0"
        val imageUrl = medicine["imageUrl"]?.toString() ?: ""
        val pharmacistId = medicine["pharmacistId"]?.toString() ?: ""
        val medicineId = medicine["id"]?.toString() ?: ""

        val requiresPrescription = medicine["requiresPrescription"] as? Boolean ?: false

        val price = priceString.toDoubleOrNull() ?: 0.0

        holder.binding.tvName.text = name
        holder.binding.tvPrice.text = "₹$price"

        if (imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .into(holder.binding.imgMedicine)
        }

        /* =======================
           ADD TO CART BUTTON
        ======================= */

        holder.binding.btnAddToCart.setOnClickListener {

            val cartItem = CartItem(
                medicineId = medicineId,
                medicineName = name,
                price = price,
                quantity = 1,
                imageUrl = imageUrl,
                pharmacistId = pharmacistId   // 🔥 REQUIRED
            )

            CartManager.addToCart(cartItem)

            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
        }

        /* =======================
           BUY NOW BUTTON (Existing flow)
        ======================= */
        holder.binding.btnBuy.setOnClickListener {
            onBuyClick(
                mapOf(
                    "name" to name,
                    "price" to priceString,
                    "imageUrl" to imageUrl,
                    "pharmacistId" to pharmacistId,
                    "requiresPrescription" to requiresPrescription
                )
            )
        }
    }

    override fun getItemCount(): Int = medicines.size
}