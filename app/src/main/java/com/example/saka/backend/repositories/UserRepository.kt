package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.database.DatabaseReference

class UserRepository(private val dbRef: DatabaseReference) {

    private val TAG = "SakaApp.UserRepo"

    fun createUserNode(userId: String, data: Map<String, Any>) {
        val userRef = dbRef.child("users").child(userId)

        Log.d(TAG, "URL de la base : ${dbRef.root.toString()}")
        Log.d(TAG, "Tentative de création du noeud utilisateur avec ID = $userId et data = $data")

        userRef.setValue(data)
            .addOnSuccessListener {
                Log.d(TAG, "Noeud utilisateur créé AVEC SUCCES")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la création du noeud utilisateur", e)
            }
            .addOnCompleteListener { task ->
                Log.d(TAG, "Appel setValue COMPLET, réussite = ${task.isSuccessful}")
                if (!task.isSuccessful) {
                    task.exception?.let {
                        Log.e(TAG, "Exception lors du setValue", it)
                    }
                }
            }
    }

    fun getUserDistributors(userId: String, onResult: (List<String>) -> Unit) {
        val distributorsRef = dbRef.child("users").child(userId).child("distributors")
        distributorsRef.get()
            .addOnSuccessListener { snapshot ->
                val distributors = snapshot.children.mapNotNull { it.key }
                onResult(distributors)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la récupération des distributeurs", e)
                onResult(emptyList())
            }
    }
}
