package com.example.saka.backend.repositories

import com.google.firebase.database.DatabaseReference

class DistributorTriggerRepository(private val dbRef: DatabaseReference) {

    fun setTriggerNow(userId: String, distributorId: String, value: Boolean) {
        dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("triggerNow")
            .setValue(value)
    }
}
