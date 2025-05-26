package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository spécialisé dans l'assignation des distributeurs aux utilisateurs.
 *
 * Gère la vérification d'existence du distributeur,
 * la vérification qu'il n'est pas déjà assigné,
 * puis l'attribution à un utilisateur dans Firestore.
 */
class DistributorAssignmentRepository(private val db: FirebaseFirestore) {

    private val TAG = "SakaApp.AssignRepo"

    /**
     * Associe un distributeur (par son ID) à un utilisateur.
     * Vérifie que le distributeur existe et n'est pas déjà attribué.
     * Met à jour le champ "assignedTo" dans le document distributeur,
     * puis ajoute une référence dans la sous-collection "distributors" de l'utilisateur.
     *
     * @param userId ID de l'utilisateur.
     * @param distributorId ID du distributeur.
     * @param onResult Callback indiquant le succès (true) ou l'échec (false) de l'opération.
     */
    fun assignDistributorToUser(
        userId: String,
        distributorId: String,
        onResult: (success: Boolean) -> Unit
    ) {
        val distributorRef = db.collection("distributors").document(distributorId)

        distributorRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.e(TAG, "Distributor $distributorId does not exist")
                    onResult(false)
                    return@addOnSuccessListener
                }

                val assignedTo = document.getString("assignedTo")
                if (assignedTo != null) {
                    Log.e(TAG, "Distributor $distributorId is already assigned to $assignedTo")
                    onResult(false)
                    return@addOnSuccessListener
                }

                // Mise à jour pour assigner le distributeur
                distributorRef.update("assignedTo", userId)
                    .addOnSuccessListener {
                        // Ajout d'une référence dans la collection utilisateur
                        db.collection("users")
                            .document(userId)
                            .collection("distributors")
                            .document(distributorId)
                            .set(mapOf("assignedAt" to System.currentTimeMillis()))
                            .addOnSuccessListener {
                                Log.d(TAG, "Distributor $distributorId assigned to user $userId successfully")
                                onResult(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to add distributor reference for user $userId", e)
                                onResult(false)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update distributor $distributorId", e)
                        onResult(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get distributor $distributorId", e)
                onResult(false)
            }
    }
}
