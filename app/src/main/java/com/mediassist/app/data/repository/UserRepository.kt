package com.mediassist.app.data.repository
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.data.model.User

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveUserProfile(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUserProfile(uid: String, onResult: (User?) -> Unit) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult(doc.toObject(User::class.java))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
