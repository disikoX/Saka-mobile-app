package com.example.saka.backend

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "SakaApp"

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
}
