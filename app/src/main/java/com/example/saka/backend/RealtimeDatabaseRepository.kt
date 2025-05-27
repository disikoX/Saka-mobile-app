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

    /**
     * Crée un noeud utilisateur dans la base de données avec les données spécifiées.
     * Généralement appelé lors de l'inscription.
     */
    fun createUserNode(userId: String, data: Map<String, Any>) {
        userRepo.createUserNode(userId, data)
    }

    /**
     * Récupère la liste des distributeurs assignés à un utilisateur donné.
     * Utilisé pour afficher les distributeurs liés à l'utilisateur.
     */
    fun getUserDistributors(userId: String, onResult: (List<String>) -> Unit) {
        userRepo.getUserDistributors(userId, onResult)
    }

    // ----------------------- ASSIGNATION ------------------------

    /**
     * Assigne un distributeur à un utilisateur si ce distributeur existe
     * et n'est pas déjà attribué. Retourne true si succès.
     */
    fun assignDistributorToUser(userId: String, distributorId: String, onResult: (Boolean) -> Unit) {
        distributorAssignRepo.assignDistributorToUser(userId, distributorId, onResult)
    }

    // ----------------------- PARAMÈTRES ------------------------

    /**
     * Définit une nouvelle quantité de croquettes dans la base pour le distributeur.
     */
    fun setQuantity(distributorId: String, quantity: Int) {
        settingsRepo.setQuantity(distributorId, quantity)
    }

    /**
     * Récupère la quantité actuelle définie dans la base pour le distributeur.
     */
    fun getQuantity(distributorId: String, onResult: (Int?) -> Unit) {
        settingsRepo.getQuantity(distributorId, onResult)
    }

    /**
     * Définit un seuil critique (valeur minimale) pour le niveau de croquettes du distributeur.
     */
    fun setCriticalThreshold(distributorId: String, threshold: Int) {
        settingsRepo.setCriticalThreshold(distributorId, threshold)
    }

    /**
     * Récupère le seuil critique actuel du distributeur (s'il est défini).
     */
    fun getCriticalThreshold(distributorId: String, onResult: (Int?) -> Unit) {
        settingsRepo.getCriticalThreshold(distributorId, onResult)
    }

    // ----------------------- MÉTRIQUES ------------------------

    /**
     * Met à jour le poids actuel détecté dans le bol du distributeur (en grammes).
     */
    fun setCurrentWeight(distributorId: String, weight: Float) {
        metricsRepo.setCurrentWeight(distributorId, weight)
    }

    /**
     * Récupère le poids actuel détecté par le distributeur.
     */
    fun getCurrentWeight(distributorId: String, onResult: (Float?) -> Unit) {
        metricsRepo.getCurrentWeight(distributorId, onResult)
    }

    /**
     * Vérifie si le poids actuel est en dessous ou égal au seuil critique.
     * Cette méthode combine les appels à getCurrentWeight et getCriticalThreshold.
     */
    fun checkIfWeightIsCritical(distributorId: String, onResult: (Boolean?) -> Unit) {
        Log.d(TAG, "Début checkIfWeightIsCritical pour $distributorId")

        // Étape 1 : récupérer le poids actuel
        getCurrentWeight(distributorId) { currentWeight ->
            if (currentWeight == null) {
                Log.e(TAG, "Poids actuel NON trouvé pour $distributorId")
                onResult(null)
                return@getCurrentWeight
            }
            Log.d(TAG, "Poids actuel récupéré: $currentWeight")

            // Étape 2 : récupérer le seuil critique
            getCriticalThreshold(distributorId) { criticalThreshold ->
                if (criticalThreshold == null) {
                    Log.e(TAG, "Seuil critique NON trouvé pour $distributorId")
                    onResult(null)
                    return@getCriticalThreshold
                }
                Log.d(TAG, "Seuil critique récupéré: $criticalThreshold")

                // Étape 3 : comparaison
                val isCritical = currentWeight <= criticalThreshold
                Log.d(TAG, "Comparaison poids <= seuil ? $isCritical")
                onResult(isCritical)
            }
        }
    }
}