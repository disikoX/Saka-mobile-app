package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository spécialisé dans la gestion des données utilisateurs dans Firestore.
 * Inclut création de document utilisateur et récupération des distributeurs associés.
 */
class UserRepository(private val db: FirebaseFirestore) {

    private val TAG = "SakaApp.UserRepo"

    /**
     * Crée un document utilisateur avec un ID donné dans la collection "users".
     * Utilisé après l'inscription pour initialiser le profil utilisateur.
     *
     * @param userId ID unique de l'utilisateur.
     * @param data Données initiales à stocker dans le document utilisateur.
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
     * Récupère tous les IDs des distributeurs associés à un utilisateur donné.
     *
     * @param userId ID de l'utilisateur.
     * @param onResult Callback avec la liste des IDs des distributeurs.
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
}
