package com.example.saka.backend

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.saka.backend.repositories.UserRepository
import com.example.saka.backend.repositories.DistributorAssignmentRepository
import com.example.saka.backend.repositories.DistributorSettingsRepository
import com.example.saka.backend.repositories.DistributorMetricsRepository

/**
 * FirestoreRepository agit comme une façade principale pour la gestion
 * des opérations Firestore liées à l'application.
 */
class FirestoreRepository {

    private val TAG = "FirestoreRepository"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Instanciation des repositories spécialisés
    private val userRepo = UserRepository(db)
    private val distributorAssignRepo = DistributorAssignmentRepository(db)
    private val settingsRepo = DistributorSettingsRepository(db, auth)
    private val metricsRepo = DistributorMetricsRepository(db, auth)

    // ----------------------- UTILISATEUR ------------------------

    fun createUserDocument(userId: String, data: Map<String, Any>) {
        userRepo.createUserDocument(userId, data)
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

    /**
     * Vérifie si le poids actuel du distributeur est inférieur ou égal au seuil critique.
     *
     * @param distributorId ID du distributeur.
     * @param onResult Callback avec :
     *    - true si poids <= seuil critique,
     *    - false si poids > seuil critique,
     *    - null si impossible de déterminer (données manquantes).
     */
    fun checkIfWeightIsCritical(distributorId: String, onResult: (Boolean?) -> Unit) {
        Log.d(TAG, "Début checkIfWeightIsCritical pour $distributorId")

        getCurrentWeight(distributorId) { currentWeight ->
            if (currentWeight == null) {
                Log.e(TAG, "Poids actuel NON trouvé pour $distributorId")
                onResult(null)
                return@getCurrentWeight
            }
            Log.d(TAG, "Poids actuel récupéré: $currentWeight")

            settingsRepo.getCriticalThreshold(distributorId) { criticalThreshold ->
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
