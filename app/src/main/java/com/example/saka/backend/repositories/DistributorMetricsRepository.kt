package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class DistributorMetricsRepository(
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth
) {

    private val TAG = "SakaApp.MetricsRepo"

    fun getCurrentWeight(distributorId: String, onResult: (Float?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getCurrentWeight: User not authenticated")
            onResult(null)
            return
        }

        val weightRef = dbRef.child("users").child(userId).child("distributors").child(distributorId).child("currentWeight")
        weightRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val value = snapshot.getValue(Float::class.java)
                    if (value != null) {
                        Log.d(TAG, "✅ Weight found: $value g for $distributorId")
                        onResult(value)
                    } else {
                        Log.e(TAG, "⚠️ Weight value is null or invalid")
                        onResult(null)
                    }
                } else {
                    Log.e(TAG, "⚠️ currentWeight does not exist for $distributorId")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get current weight for $distributorId", e)
                onResult(null)
            }
    }
}
