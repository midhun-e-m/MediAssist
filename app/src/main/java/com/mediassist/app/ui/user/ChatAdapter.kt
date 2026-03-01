package com.mediassist.app.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mediassist.app.data.model.ChatMessage
import com.mediassist.app.databinding.ItemChatAiBinding
import com.mediassist.app.databinding.ItemChatUserBinding

class ChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == 1) {
            val binding = ItemChatUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            UserViewHolder(binding)
        } else {
            val binding = ItemChatAiBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AiViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is UserViewHolder) {
            holder.binding.tvMessage.text = message.message
        } else if (holder is AiViewHolder) {
            holder.binding.tvMessage.text = message.message
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class UserViewHolder(val binding: ItemChatUserBinding)
        : RecyclerView.ViewHolder(binding.root)

    class AiViewHolder(val binding: ItemChatAiBinding)
        : RecyclerView.ViewHolder(binding.root)
}
