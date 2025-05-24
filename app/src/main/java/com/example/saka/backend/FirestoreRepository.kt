package com.example.saka.backend

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "SakaApp"

    // -------------------------------------------------------------------------
    // 🔐 UTILISATEUR : Création et lecture de documents utilisateur
    // -------------------------------------------------------------------------

    /**
     * Crée un document utilisateur avec un ID donné dans la collection "users".
     * Utilisé après l'inscription pour initialiser le profil utilisateur.
     */
    fun createUserDocument(userId: String, data: Map<String, Any>) {
        db.collection("users").document(userId)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "User document created")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user doc", e)
            }
    }

    /**
     * Récupère tous les distributeurs associés à un utilisateur donné.
     * Les distributeurs sont stockés dans la sous-collection "distributors" du document utilisateur.
     */
    fun getUserDistributors(userId: String, onResult: (List<String>) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("distributors")
            .get()
            .addOnSuccessListener { snapshot ->
                val distributors = snapshot.documents.mapNotNull { it.id }
                onResult(distributors)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user distributors", e)
                onResult(emptyList())
            }
    }

    // -------------------------------------------------------------------------
    // 🤝 ASSIGNATION : Assignation d'un distributeur à un utilisateur
    // -------------------------------------------------------------------------

    /**
     * Associe un distributeur (par son ID) à un utilisateur.
     * Vérifie d'abord que le distributeur existe et n'est pas déjà attribué.
     * Met à jour le champ "assignedTo" dans le document distributeur,
     * puis ajoute une référence dans la sous-collection de l'utilisateur.
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
                    onResult(false)
                    return@addOnSuccessListener
                }

                val assignedTo = document.getString("assignedTo")
                if (assignedTo != null) {
                    onResult(false)
                    return@addOnSuccessListener
                }

                distributorRef.update("assignedTo", userId)
                    .addOnSuccessListener {
                        db.collection("users")
                            .document(userId)
                            .collection("distributors")
                            .document(distributorId)
                            .set(mapOf("assignedAt" to System.currentTimeMillis()))
                            .addOnSuccessListener {
                                onResult(true)
                            }
                            .addOnFailureListener {
                                onResult(false)
                            }
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    // -------------------------------------------------------------------------
    // ⚙️ PARAMÈTRES : Gestion de la ration (quantity) pour un distributeur
    // -------------------------------------------------------------------------

    /**
     * Enregistre la quantité de croquettes (en grammes) à distribuer dans les paramètres
     * du distributeur (stockée dans une sous-collection "settings").
     * Vérifie d'abord que la quantité est raisonnable et que l'utilisateur est authentifié.
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
     * La valeur est stockée dans le document "quantity" de la sous-collection "settings".
     */
    fun getQuantity(distributorId: String, onResult: (quantity: Int?) -> Unit) {
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
}
