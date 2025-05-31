package com.example.saka.backend.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DistributorPlanningRepository(private val dbRef: DatabaseReference) {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isValidTimeFormat(time: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            LocalTime.parse(time, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isValidPlanningData(data: Map<String, Any>): Boolean {
        val time = data["time"] as? String ?: return false
        if (data["enabled"] !is Boolean) return false

        return isValidTimeFormat(time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPlanning(
        userId: String,
        distributorId: String,
        planningData: Map<String, Any>,
        onComplete: (success: Boolean, planningId: String?) -> Unit
    ) {
        if (!isValidPlanningData(planningData)) {
            onComplete(false, null)
            return
        }

        val newRef = dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")
            .push()

        val planningId = newRef.key
        if (planningId == null) {
            onComplete(false, null)
            return
        }

        newRef.setValue(planningData)
            .addOnSuccessListener { onComplete(true, planningId) }
            .addOnFailureListener { onComplete(false, null) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePlanning(
        userId: String,
        distributorId: String,
        planningId: String,
        planningData: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        if (!isValidPlanningData(planningData)) {
            onComplete(false)
            return
        }

        dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")
            .child(planningId)
            .setValue(planningData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deletePlanning(
        userId: String,
        distributorId: String,
        planningId: String,
        onComplete: (Boolean) -> Unit
    ) {
        dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")
            .child(planningId)
            .removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
