package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository dédié à la gestion des données dynamiques (métriques)
 * des distributeurs, comme le poids actuel du réservoir.
 *
 * Ces données sont stockées dans la sous-collection "metrics" du distributeur.
 */
class DistributorMetricsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val TAG = "SakaApp.MetricsRepo"

    /**
     * Met à jour le poids actuel du réservoir (en grammes) pour un distributeur.
     * Le poids ne peut pas être négatif.
     */
    fun setCurrentWeight(distributorId: String, weight: Float) {
        if (weight < 0f) {
            Log.e(TAG, "setCurrentWeight: Weight cannot be negative")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "setCurrentWeight: User not authenticated")
            return
        }

        val weightRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("metrics")
            .document("currentWeight")

        val data = mapOf(
            "value" to weight,
            "updatedAt" to System.currentTimeMillis()
        )

        weightRef.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Current weight updated for $distributorId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to update weight for $distributorId", e)
            }
    }

    /**
     * Récupère le poids actuel du réservoir pour un distributeur donné.
     */
    fun getCurrentWeight(distributorId: String, onResult: (Float?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getCurrentWeight: User not authenticated")
            onResult(null)
            return
        }

        val weightRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("metrics")
            .document("currentWeight")

        Log.d(TAG, "Fetching weight for userId=$userId, distributorId=$distributorId")

        weightRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val value = document.getDouble("value")
                    if (value != null) {
                        Log.d(TAG, "✅ Weight found: $value")
                        onResult(value.toFloat())
                    } else {
                        Log.e(TAG, "⚠️ Document found but 'value' field is missing or null")
                        onResult(null)
                    }
                } else {
                    Log.e(TAG, "⚠️ Document does not exist")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get current weight for $distributorId", e)
                onResult(null)
            }
    }

}
