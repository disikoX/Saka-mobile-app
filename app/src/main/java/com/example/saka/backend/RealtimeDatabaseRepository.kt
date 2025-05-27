package com.example.saka.backend

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.saka.backend.repositories.UserRepository
import com.example.saka.backend.repositories.DistributorAssignmentRepository
import com.example.saka.backend.repositories.DistributorSettingsRepository
import com.example.saka.backend.repositories.DistributorMetricsRepository

/**
 * RealtimeDatabaseRepository agit comme façade principale pour la gestion
 * des opérations liées à Firebase Realtime Database dans l'application.
 */
class RealtimeDatabaseRepository {

    private val TAG = "RealtimeDatabaseRepository"

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // Repositories spécialisés avec Firebase Realtime Database
    private val userRepo = UserRepository(dbRef)
    private val distributorAssignRepo = DistributorAssignmentRepository(dbRef)
    private val settingsRepo = DistributorSettingsRepository(dbRef, auth)
    private val metricsRepo = DistributorMetricsRepository(dbRef, auth)

    // ----------------------- UTILISATEUR ------------------------

    fun createUserDocument(userId: String, data: Map<String, Any>) {
        userRepo.createUserNode(userId, data)
    }

    fun getUserDistributors(userId: String, onResult: (List<String>) -> Unit) {
        userRepo.getUserDistributors(userId, onResult)
    }

    // ----------------------- ASSIGNATION ------------------------

    fun assignDistributorToUser(userId: String, distributorId: String, onResult: (Boolean) -> Unit) {
        distributorAssignRepo.assignDistributorToUser(userId, distributorId, onResult)
    }

    // ----------------------- PARAMÈTRES ------------------------

    fun setQuantity(distributorId: String, quantity: Int) {
        settingsRepo.setQuantity(distributorId, quantity)
    }

    fun getQuantity(distributorId: String, onResult: (Int?) -> Unit) {
        settingsRepo.getQuantity(distributorId, onResult)
    }

    fun setCriticalThreshold(distributorId: String, threshold: Int) {
        settingsRepo.setCriticalThreshold(distributorId, threshold)
    }

    fun getCriticalThreshold(distributorId: String, onResult: (Int?) -> Unit) {
        settingsRepo.getCriticalThreshold(distributorId, onResult)
    }

    // ----------------------- MÉTRIQUES ------------------------

    fun setCurrentWeight(distributorId: String, weight: Float) {
        metricsRepo.setCurrentWeight(distributorId, weight)
    }

    fun getCurrentWeight(distributorId: String, onResult: (Float?) -> Unit) {
        metricsRepo.getCurrentWeight(distributorId, onResult)
    }

    fun checkIfWeightIsCritical(distributorId: String, onResult: (Boolean?) -> Unit) {
        Log.d(TAG, "Début checkIfWeightIsCritical pour $distributorId")

        getCurrentWeight(distributorId) { currentWeight ->
            if (currentWeight == null) {
                Log.e(TAG, "Poids actuel NON trouvé pour $distributorId")
                onResult(null)
                return@getCurrentWeight
            }
            Log.d(TAG, "Poids actuel récupéré: $currentWeight")

            getCriticalThreshold(distributorId) { criticalThreshold ->
                if (criticalThreshold == null) {
                    Log.e(TAG, "Seuil critique NON trouvé pour $distributorId")
                    onResult(null)
                    return@getCriticalThreshold
                }
                Log.d(TAG, "Seuil critique récupéré: $criticalThreshold")

                val isCritical = currentWeight <= criticalThreshold
                Log.d(TAG, "Comparaison poids <= seuil ? $isCritical")
                onResult(isCritical)
            }
        }
    }
}
