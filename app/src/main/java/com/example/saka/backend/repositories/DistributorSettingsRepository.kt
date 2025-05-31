package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class DistributorSettingsRepository(
    private val dbRef: DatabaseReference,
    private val auth: FirebaseAuth
) {

    private val TAG = "SakaApp.SettingsRepo"

    fun setQuantity(distributorId: String, quantity: Int) {
        if (quantity > 1000) {
            Log.e(TAG, "setQuantity: Quantity exceeds maximum allowed value (1000g)")
            return
        }

        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "setQuantity: User not authenticated")
            return
        }

        val quantityPath = "users/$userId/distributors/$distributorId/settings/quantity"
        dbRef.child(quantityPath).setValue(quantity)
            .addOnSuccessListener {
                Log.d(TAG, "Quantity set successfully for $distributorId: $quantity g")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to set quantity for $distributorId", e)
            }
    }

    fun getQuantity(distributorId: String, onResult: (Int?) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "getQuantity: User not authenticated")
            onResult(null)
            return
        }

        val quantityPath = "users/$userId/distributors/$distributorId/settings/quantity"
        dbRef.child(quantityPath).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val quantity = snapshot.getValue(Int::class.java)
                    onResult(quantity)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get quantity for $distributorId", e)
                onResult(null)
            }
    }

    fun setCriticalThreshold(distributorId: String, threshold: Int) {
        if (threshold < 0) {
            Log.e(TAG, "setCriticalThreshold: Threshold cannot be negative")
            return
        }

        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "setCriticalThreshold: User not authenticated")
            return
        }

        val thresholdPath = "users/$userId/distributors/$distributorId/settings/criticalThreshold"
        dbRef.child(thresholdPath).setValue(threshold)
            .addOnSuccessListener {
                Log.d(TAG, "Critical threshold set successfully for $distributorId: $threshold g")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to set critical threshold for $distributorId", e)
            }
    }

    fun getCriticalThreshold(distributorId: String, onResult: (Int?) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "getCriticalThreshold: User not authenticated")
            onResult(null)
            return
        }

        val thresholdPath = "users/$userId/distributors/$distributorId/settings/criticalThreshold"
        dbRef.child(thresholdPath).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val threshold = snapshot.getValue(Int::class.java)
                    onResult(threshold)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get critical threshold for $distributorId", e)
                onResult(null)
            }
    }
}
