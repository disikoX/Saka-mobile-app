package com.example.saka.backend

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.saka.backend.repositories.UserRepository
import com.example.saka.backend.repositories.DistributorAssignmentRepository
import com.example.saka.backend.repositories.DistributorSettingsRepository
// import com.example.saka.backend.repositories.DistributorMetricsRepository
import com.example.saka.backend.repositories.DistributorObserverRepository
import com.example.saka.backend.repositories.DistributorPlanningRepository
import com.example.saka.backend.repositories.DistributorTriggerRepository
import com.example.saka.backend.repositories.DistributorRepository
import com.example.saka.backend.repositories.HistoryEntry
import com.example.saka.backend.repositories.HistoryRepository
import com.example.saka.backend.repositories.SuccessStats

/**
 * RealtimeDatabaseRepository agit comme façade principale pour la gestion
 * des opérations liées à Firebase Realtime Database dans l'application.
 */
class RealtimeDatabaseRepository {

    private val dbRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    // Repositories spécialisés avec Firebase Realtime Database
    private val userRepo = UserRepository(dbRef)
    private val distributorAssignRepo = DistributorAssignmentRepository(dbRef)
    private val settingsRepo = DistributorSettingsRepository(dbRef, auth)
//    private val metricsRepo = DistributorMetricsRepository(dbRef, auth)
    private val observerRepo = DistributorObserverRepository(dbRef)
    private val planningRepo = DistributorPlanningRepository(dbRef)
    private val triggerRepo = DistributorTriggerRepository(dbRef)
    val distributorRepo=DistributorRepository(dbRef)
    val historyRepo=HistoryRepository(dbRef)

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
     * Récupère le poids actuel détecté par le distributeur.
     */
//    fun getCurrentWeight(distributorId: String, onResult: (Float?) -> Unit) {
//        metricsRepo.getCurrentWeight(distributorId, onResult)
//    }

    // ----------------------- OBSERVATION TEMPS RÉEL ------------------------

    /**
     * Active une écoute en temps réel sur la valeur du poids actuel d’un distributeur.
     * À chaque changement détecté dans la base, la fonction [onWeightChanged] est appelée avec la nouvelle valeur.
     * En cas d’erreur d’accès à la base, [onError] est invoqué avec le détail de l’erreur.
     * Retourne un handle permettant de supprimer l'écoute ultérieurement via `.remove()`.
     */
    fun observeCurrentWeight(
        userId: String,
        distributorId: String,
        onWeightChanged: (Int) -> Unit,
        onError: (DatabaseError) -> Unit
    ): DistributorObserverRepository.WeightListenerHandle {
        return observerRepo.observeCurrentWeight(userId, distributorId, onWeightChanged, onError)
    }

    // ----------------------- PLANNING ------------------------

    /**
     * Crée un nouveau planning avec un ID auto-généré.
     * Retourne l'ID créé et un booléen succès via onComplete.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createPlanning(
        userId: String,
        distributorId: String,
        planningData: Map<String, Any>,
        onComplete: (success: Boolean, planningId: String?) -> Unit
    ) {
        planningRepo.createPlanning(userId, distributorId, planningData, onComplete)
    }

    /**
     * Configure la pause (durée et activation) dans le planning d’un distributeur.
     */
    fun configureBreak(
        userId: String,
        distributorId: String,
        duration: Int,
        active: Boolean,
        onComplete: (success: Boolean) -> Unit
    ) {
        planningRepo.configureBreak(userId, distributorId, duration, active, onComplete)
    }


    /**
     * Met à jour un planning existant identifié par planningId.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePlanning(
        userId: String,
        distributorId: String,
        planningId: String,
        planningData: Map<String, Any>,
        onComplete: (Boolean) -> Unit
    ) {
        planningRepo.updatePlanning(userId, distributorId, planningId, planningData, onComplete)
    }

    /**
     * Supprime un planning d’un distributeur d’un utilisateur.
     */
    fun deletePlanning(
        userId: String,
        distributorId: String,
        planningId: String,
        onComplete: (Boolean) -> Unit
    ) {
        planningRepo.deletePlanning(userId, distributorId, planningId, onComplete)
    }

    /**
     * Récupère la prochaine heure de distribution active, en tenant compte des plannings du distributeur.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextDistributionTime(
        userId: String,
        distributorId: String,
        onResult: (nextTime: String?) -> Unit
    ) {
        planningRepo.getNextDistributionTime(userId, distributorId, onResult)
    }

    /**
     * Récupère les informations de la pause configurée pour un distributeur.
     */
    fun getBreakInfos(
        userId: String,
        distributorId: String,
        onResult: (duration: Int?, active: Boolean?) -> Unit
    ) {
        planningRepo.getBreakInfos(userId, distributorId, onResult)
    }

    /**
     * Récupère la liste des plannings (hors pause) pour un distributeur donné.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPlannings(
        userId: String,
        distributorId: String,
        onResult: (plannings: Map<String, Map<String, Any>>) -> Unit
    ) {
        planningRepo.getPlannings(userId, distributorId, onResult)
    }



    // ----------------------- TRIGGER MANUEL ------------------------

    /**
     * Déclenche manuellement la distribution de croquettes en définissant
     * le champ `triggerNow` à true dans la base. L’ESP32 se chargera de
     * remettre ce champ à false une fois la distribution effectuée.
     */
    fun triggerNow(userId: String, distributorId: String) {
        triggerRepo.setTriggerNow(userId, distributorId, true)
    }

    // ----------------------- DISTRIBUTEUR ------------------------

    /**
     * Récupère le statut du distributeur
     */
    fun getDistributorStatus(distributorId: String, onResult: (String?) -> Unit) {
        distributorRepo.getDistributorStatus(distributorId) { status ->
            onResult(status)
        }
    }

    /**
     * Récupère la capacité du distributeur
     */
    fun getCapacity(distributorId: String, onResult: (Int?) -> Unit) {
        distributorRepo.getCapacity(distributorId) { capacity ->
            onResult(capacity)
        }
    }

    // ----------------------- HISTORIQUE ------------------------
    fun getSuccessStats(
        userId: String,
        distributorId: String,
        onResult: (SuccessStats) -> Unit
    ) {
        historyRepo.getSuccessStats(userId, distributorId) { stats ->
            // Tu peux traiter les stats ici si besoin
            onResult(stats)  // transmet le résultat à l’appelant
        }
    }

    fun getHistory(
        userId: String,
        distributorId: String,
        onResult: (List<HistoryEntry>) -> Unit
    ) {
        historyRepo.getHistory(userId, distributorId) { historyList ->
            // Tu peux faire un log ici si tu veux
            for (entry in historyList) {
                Log.d("HistoryViewModel", "Entry: success=${entry.success}, time=${entry.time}, quantity=${entry.quantity}")
            }

            // Transmettre à l'appelant
            onResult(historyList)
        }
    }


}