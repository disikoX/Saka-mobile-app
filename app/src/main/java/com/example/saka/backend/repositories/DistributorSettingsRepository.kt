package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository spécialisé dans la gestion des paramètres des distributeurs,
 * tels que la quantité de croquettes à distribuer et le seuil critique personnalisé.
 *
 * Utilise FirebaseAuth pour récupérer l'utilisateur courant,
 * et Firestore pour lire/écrire dans la sous-collection "settings" de chaque distributeur.
 */
class DistributorSettingsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val TAG = "SakaApp.SettingsRepo"

    /**
     * Enregistre la quantité de croquettes (en grammes) à distribuer dans les paramètres
     * du distributeur (limité à 1000g max).
     *
     * @param distributorId ID du distributeur.
     * @param quantity Quantité en grammes.
     */
    fun setQuantity(distributorId: String, quantity: Int) {
        if (quantity > 1000) {
            Log.e(TAG, "setQuantity: Quantity exceeds maximum allowed value (1000g)")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "setQuantity: User not authenticated")
            return
        }

        val quantityRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("settings")
            .document("quantity")

        val data = mapOf("value" to quantity)

        quantityRef.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Quantity set successfully for $distributorId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to set quantity for $distributorId", e)
            }
    }

    /**
     * Récupère la quantité de croquettes configurée pour un distributeur donné.
     *
     * @param distributorId ID du distributeur.
     * @param onResult Callback avec la quantité (ou null si non définie).
     */
    fun getQuantity(distributorId: String, onResult: (Int?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getQuantity: User not authenticated")
            onResult(null)
            return
        }

        val quantityRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("settings")
            .document("quantity")

        quantityRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val quantity = document.getLong("value")?.toInt()
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

    /**
     * Enregistre le seuil critique personnalisé (en grammes) pour un distributeur.
     * Le seuil ne peut pas être négatif.
     *
     * @param distributorId ID du distributeur.
     * @param threshold Seuil critique en grammes.
     */
    fun setCriticalThreshold(distributorId: String, threshold: Int) {
        if (threshold < 0) {
            Log.e(TAG, "setCriticalThreshold: Threshold cannot be negative")
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "setCriticalThreshold: User not authenticated")
            return
        }

        val thresholdRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("settings")
            .document("criticalThreshold")

        val data = mapOf("value" to threshold)

        thresholdRef.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Critical threshold set successfully for $distributorId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to set critical threshold for $distributorId", e)
            }
    }

    /**
     * Récupère le seuil critique configuré pour un distributeur donné.
     *
     * @param distributorId ID du distributeur.
     * @param onResult Callback avec le seuil (ou null si non défini).
     */
    fun getCriticalThreshold(distributorId: String, onResult: (Int?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getCriticalThreshold: User not authenticated")
            onResult(null)
            return
        }

        val thresholdRef = db.collection("users")
            .document(userId)
            .collection("distributors")
            .document(distributorId)
            .collection("settings")
            .document("criticalThreshold")

        thresholdRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val threshold = document.getLong("value")?.toInt()
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
