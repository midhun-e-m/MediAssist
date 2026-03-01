package com.mediassist.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSource {
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}
