package com.mediassist.app.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mediassist.app.databinding.ItemTrackingBinding

class TrackingAdapter(
    private val list: List<TrackingItem>
) : RecyclerView.Adapter<TrackingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTrackingBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemTrackingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.tvStatus.text = item.status
        holder.binding.tvLocation.text = item.location
        holder.binding.tvTime.text = item.time
    }
}