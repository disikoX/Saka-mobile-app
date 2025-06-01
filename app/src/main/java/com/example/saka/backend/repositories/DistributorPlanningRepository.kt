package com.example.saka.backend.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalDateTime
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

    fun configureBreak(
        userId: String,
        distributorId: String,
        duration: Int,
        active: Boolean,
        onComplete: (success: Boolean) -> Unit
    ) {
        val breakData = mapOf(
            "duration" to duration,
            "active" to active
        )

        val breakPath = "users/$userId/distributors/$distributorId/planning/break"

        dbRef.child(breakPath).setValue(breakData)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextDistributionTime(
        userId: String,
        distributorId: String,
        onResult: (nextTime: String?) -> Unit
    ) {
        val planningRef = dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")

        planningRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = LocalTime.now()
                val today = LocalDate.now()
                var nextDateTime: LocalDateTime? = null

                for (planningSnapshot in snapshot.children) {
                    val key = planningSnapshot.key ?: continue
                    if (key == "break") continue

                    val timeStr = planningSnapshot.child("time").getValue(String::class.java)
                    val enabled = planningSnapshot.child("enabled").getValue(Boolean::class.java) ?: false

                    if (!enabled || timeStr.isNullOrBlank()) continue

                    try {
                        val planningTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                        var candidateDateTime = LocalDateTime.of(today, planningTime)

                        // Si l’heure est déjà passée aujourd’hui, considérer le lendemain
                        if (candidateDateTime.isBefore(LocalDateTime.of(today, now))) {
                            candidateDateTime = candidateDateTime.plusDays(1)
                        }

                        if (nextDateTime == null || candidateDateTime.isBefore(nextDateTime)) {
                            nextDateTime = candidateDateTime
                        }

                    } catch (e: DateTimeParseException) {
                        Log.e("PlanningRepository", "Invalid time format in planning: $timeStr")
                    }
                }

                val result = nextDateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm"))
                onResult(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlanningRepository", "Error fetching planning: ${error.message}")
                onResult(null)
            }
        })
    }


    fun getBreakInfos(
        userId: String,
        distributorId: String,
        onResult: (duration: Int?, active: Boolean?) -> Unit
    ) {
        val breakRef = dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")
            .child("break")

        breakRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val duration = snapshot.child("duration").getValue(Int::class.java)
                val active = snapshot.child("active").getValue(Boolean::class.java)
                onResult(duration, active)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlanningRepository", "Error fetching break info: ${error.message}")
                onResult(null, null)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPlannings(
        userId: String,
        distributorId: String,
        onResult: (plannings: Map<String, Map<String, Any>>) -> Unit
    ) {
        val planningRef = dbRef.child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("planning")

        planningRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val planningMap = mutableMapOf<String, Map<String, Any>>()

                for (planningSnapshot in snapshot.children) {
                    val key = planningSnapshot.key ?: continue
                    if (key == "break") continue

                    val time = planningSnapshot.child("time").getValue(String::class.java) ?: "00:00"
                    val enabled = planningSnapshot.child("enabled").getValue(Boolean::class.java) ?: false

                    planningMap[key] = mapOf(
                        "time" to time,
                        "enabled" to enabled
                    )
                }

                onResult(planningMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlanningRepository", "Error fetching plannings: ${error.message}")
                onResult(emptyMap())
            }
        })
    }


}
