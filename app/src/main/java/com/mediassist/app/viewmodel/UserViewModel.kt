package com.mediassist.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.data.model.User

class UserViewModel : ViewModel() {

    val userProfile = MutableLiveData<User>()
    val isSaved = MutableLiveData<Boolean>()

    private val db = FirebaseFirestore.getInstance()

    fun loadUserProfile(uid: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userProfile.value = doc.toObject(User::class.java)
                } else {
                    userProfile.value = User(uid = uid)
                }
            }
    }

    fun saveUserProfile(user: User) {
        db.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                isSaved.value = true
            }
            .addOnFailureListener {
                isSaved.value = false
            }
    }
}
