package com.mediassist.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.mediassist.app.data.model.EmergencyRequest

class EmergencyRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()

    // CREATE EMERGENCY
    fun createEmergency(
        request: EmergencyRequest,
        onSuccess: (String) -> Unit
    ) {
        val ref = db.collection("emergencies").document()

        ref.set(request.copy(id = ref.id, status = "PENDING"))
            .addOnSuccessListener { onSuccess(ref.id) }
    }

    // LISTEN TO ONE EMERGENCY (For IncomingRequestActivity)
    fun listenToEmergency(
        emergencyId: String,
        onUpdate: (EmergencyRequest) -> Unit
    ) {
        db.collection("emergencies")
            .document(emergencyId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.toObject(EmergencyRequest::class.java)
                    ?.let(onUpdate)
            }
    }

    // LISTEN TO ALL PENDING EMERGENCIES (For DriverDashboard)
    fun listenForPendingEmergencies(
        onNewRequest: (EmergencyRequest, String) -> Unit
    ) {
        db.collection("emergencies")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshots, _ ->

                if (snapshots == null) return@addSnapshotListener

                for (doc in snapshots.documents) {
                    val emergency = doc.toObject(EmergencyRequest::class.java)
                    emergency?.let {
                        onNewRequest(it, doc.id)
                    }
                }
            }
    }

    // ACCEPT EMERGENCY
    fun acceptEmergency(
        emergencyId: String,
        driverId: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val emergencyRef = db.collection("emergencies").document(emergencyId)
        val driverRef = db.collection("drivers").document(driverId)

        db.runTransaction { tx ->
            val snap = tx.get(emergencyRef)

            if (snap.getString("status") != "PENDING") {
                throw Exception("Already accepted")
            }

            tx.update(emergencyRef, mapOf(
                "status" to "ACCEPTED",
                "assignedDriverId" to driverId
            ))

            tx.update(driverRef, "availability", "BUSY")
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }
}
