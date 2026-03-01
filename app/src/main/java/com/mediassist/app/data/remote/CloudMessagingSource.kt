package com.mediassist.app.data.remote

import com.google.firebase.messaging.FirebaseMessaging

object CloudMessagingSource {

    fun getToken(onToken: (String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                onToken(token)
            }
    }
}
