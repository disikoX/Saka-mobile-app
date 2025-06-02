package com.example.saka.backend.repositories

import android.util.Log
import com.google.firebase.database.DatabaseReference
import java.util.Calendar

data class SuccessStats(
    val count: Int,
    val totalQuantity: Int,
    val latestTime: Long
)

class HistoryRepository(private val dbRef: DatabaseReference) {
    fun getSuccessStats(userId: String, distributorId: String, onResult: (SuccessStats) -> Unit) {
        val historyRef = dbRef
            .child("users")
            .child(userId)
            .child("distributors")
            .child(distributorId)
            .child("history")

        historyRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result

                var count = 0
                var totalQuantity = 0
                var latestTime = 0L

                val todayCal = Calendar.getInstance()
                Log.d("HistoryRepository", "Date actuelle : ${todayCal.time}")

                for (entry in snapshot.children) {
                    val success = entry.child("success").getValue(Boolean::class.java) ?: false
                    val time = entry.child("time").getValue(Long::class.java) ?: 0L
                    val quantity = entry.child("quantity").getValue(Int::class.java) ?: 0

                    val entryCal = Calendar.getInstance().apply { timeInMillis = time }
                    val sameDay = (entryCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)
                            && entryCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR))

                    Log.d(
                        "HistoryRepository",
                        "Entry id=${entry.key} success=$success time=${entryCal.time} quantity=$quantity sameDay=$sameDay"
                    )

                    if (success && sameDay) {
                        count++
                        totalQuantity += quantity

                        if (time > latestTime) {
                            latestTime = time
                        }
                    }
                }

                Log.d(
                    "HistoryRepository",
                    "Résultat: count=$count totalQuantity=$totalQuantity latestTime=$latestTime"
                )
                onResult(SuccessStats(count, totalQuantity, latestTime))
            } else {
                Log.e("HistoryRepository", "Erreur lors de la récupération : ${task.exception}")
                onResult(SuccessStats(0, 0, 0L))
            }
        }
    }

}
