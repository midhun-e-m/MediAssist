package com.mediassist.app.ui.user

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mediassist.app.R
import com.mediassist.app.data.model.ChatMessage
import com.mediassist.app.data.model.ChatRequest
import com.mediassist.app.data.model.Message
import com.mediassist.app.data.remote.GroqService
import kotlinx.coroutines.launch
import com.mediassist.app.BuildConfig

class ChatBotActivity : AppCompatActivity() {

    private val api = GroqService.create()
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        val recycler = findViewById<RecyclerView>(R.id.recyclerChat)
        val input = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        adapter = ChatAdapter(messages)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        btnSend.setOnClickListener {

            val userMessage = input.text.toString().trim()
            if (userMessage.isEmpty()) return@setOnClickListener

            // Add user bubble
            adapter.addMessage(ChatMessage(userMessage, true))
            recycler.scrollToPosition(messages.size - 1)
            input.text.clear()

            lifecycleScope.launch {

                val request = ChatRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = listOf(
                        Message(
                            role = "system",
                            content = """
You are a medical first aid assistant.
Provide only basic first aid advice.
Do not diagnose diseases.
If symptoms are severe, tell the user to call emergency services immediately.
Keep responses short and clear.
"""
                        ),
                        Message("user", userMessage)
                    )
                )

                try {
                    val response = api.getChatCompletion(
                        "Bearer ${BuildConfig.GROQ_API_KEY}",
                        request
                    )

                    val aiReply =
                        response.choices.firstOrNull()?.message?.content
                            ?: "No response"

                    adapter.addMessage(ChatMessage(aiReply, false))
                    recycler.scrollToPosition(messages.size - 1)
                    Log.d("API_TEST", BuildConfig.GROQ_API_KEY)
                } catch (e: Exception) {
                    adapter.addMessage(
                        ChatMessage("Error connecting to AI", false)
                    )
                }
            }
        }
    }
}
