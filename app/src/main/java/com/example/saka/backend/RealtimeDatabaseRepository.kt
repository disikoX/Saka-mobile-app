package com.example.saka.backend

import com.google.firebase.database.DatabaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.saka.backend.repositories.UserRepository
import com.example.saka.backend.repositories.DistributorAssignmentRepository
import com.example.saka.backend.repositories.DistributorSettingsRepository
// import com.example.saka.backend.repositories.DistributorMetricsRepository
import com.example.saka.backend.repositories.DistributorObserverRepository
import com.example.saka.backend.repositories.DistributorPlanningRepository

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
        onWeightChanged: (Float) -> Unit,
        onError: (DatabaseError) -> Unit
    ): DistributorObserverRepository.WeightListenerHandle {
        return observerRepo.observeCurrentWeight(userId, distributorId, onWeightChanged, onError)
    }

    // ----------------------- PLANNING ------------------------

    /**
     * Crée un nouveau planning avec un ID auto-généré.
     * Retourne l'ID créé et un booléen succès via onComplete.
     */
    fun createPlanning(
        userId: String,
        distributorId: String,
        planningData: Map<String, Any>,
        onComplete: (success: Boolean, planningId: String?) -> Unit
    ) {
        planningRepo.createPlanning(userId, distributorId, planningData, onComplete)
    }

    /**
     * Met à jour un planning existant identifié par planningId.
     */
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
}