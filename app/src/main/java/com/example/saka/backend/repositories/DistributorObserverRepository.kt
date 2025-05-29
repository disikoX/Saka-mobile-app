package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.database.*

class DistributorObserverRepository(
    private val dbRef: DatabaseReference
) {

    /**
     * Handle pour garder une référence au listener afin de pouvoir le supprimer.
     */
    data class WeightListenerHandle(
        val ref: DatabaseReference,
        val listener: ValueEventListener
    ) {
        fun remove() {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Observe le poids actuel d’un distributeur et exécute une action lorsqu’il change.
     */
    fun observeCurrentWeight(
        userId: String,
        distributorId: String,
        onWeightChanged: (Float) -> Unit,
        onError: (DatabaseError) -> Unit
    ): WeightListenerHandle {
        val weightRef = dbRef
            .child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("currentWeight")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weight = snapshot.getValue(Float::class.java)
                if (weight != null) {
                    onWeightChanged(weight)
                } else {
                    Log.w("DistributorObserver", "Poids null reçu pour $distributorId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DistributorObserver", "Erreur lors de l'écoute du poids", error.toException())
                onError(error)
            }
        }

        weightRef.addValueEventListener(listener)
        return WeightListenerHandle(weightRef, listener)
    }
}
