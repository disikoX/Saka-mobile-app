package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.database.DatabaseReference

class DistributorAssignmentRepository(private val dbRef: DatabaseReference) {

    private val TAG = "SakaApp.AssignRepo"

    fun assignDistributorToUser(
        userId: String,
        distributorId: String,
        onResult: (success: Boolean) -> Unit
    ) {
        val distributorRef = dbRef.child("distributors").child(distributorId)

        distributorRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Log.e(TAG, "Distributor $distributorId does not exist")
                onResult(false)
                return@addOnSuccessListener
            }

            val assignedTo = snapshot.child("assignedTo").getValue(String::class.java)
            if (!assignedTo.isNullOrEmpty()) {
                Log.e(TAG, "Distributor $distributorId is already assigned to $assignedTo")
                onResult(false)
                return@addOnSuccessListener
            }

            // Préparer les updates atomiques sur plusieurs chemins
            val updates = mapOf<String, Any?>(
                "/distributors/$distributorId/assignedTo" to userId,
                "/distributors/$distributorId/lastUpdate" to System.currentTimeMillis(),
                "/users/$userId/distributors/$distributorId" to true
            )

            dbRef.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Distributor $distributorId assigned to $userId successfully")
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to assign distributor $distributorId", e)
                    onResult(false)
                }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to fetch distributor $distributorId", e)
            onResult(false)
        }
    }
}
