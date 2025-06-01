package com.example.saka.backend.repositories

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log

class DistributorRepository(private val dbRef: DatabaseReference) {

    fun getDistributorStatus(distributorId: String, onResult: (String?) -> Unit) {
        val statusRef = dbRef.child("distributors").child(distributorId).child("status")

        statusRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                onResult(status)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DistributorRepository", "Database error: ${error.message}")
                onResult(null)
            }
        })
    }
}
