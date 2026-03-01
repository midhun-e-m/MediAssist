package com.mediassist.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.data.model.EmergencyContact
import com.mediassist.app.data.model.EmergencyRequest
import com.mediassist.app.data.remote.EmergencyRemoteDataSource

class EmergencyRepository {

    private val db = FirebaseFirestore.getInstance()
    private val remote = EmergencyRemoteDataSource()

    fun createEmergency(
        request: EmergencyRequest,
        onSuccess: (String) -> Unit
    ) {
        remote.createEmergency(request, onSuccess)
    }

    fun listenEmergency(
        emergencyId: String,
        onUpdate: (EmergencyRequest) -> Unit
    ) {
        remote.listenToEmergency(emergencyId, onUpdate)
    }

    fun acceptEmergency(
        emergencyId: String,
        driverId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        remote.acceptEmergency(emergencyId, driverId, onSuccess, onFailure)
    }

    fun addContact(
        uid: String,
        contact: EmergencyContact,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .add(contact)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getContacts(
        uid: String,
        onResult: (List<EmergencyContact>) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(EmergencyContact::class.java))
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
