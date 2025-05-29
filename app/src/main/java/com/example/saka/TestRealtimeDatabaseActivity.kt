package com.example.saka

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import androidx.core.app.NotificationCompat
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.backend.repositories.DistributorObserverRepository
import com.example.saka.ui.theme.SakaTheme
import com.example.saka.local.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.launch

class TestRealtimeDatabaseActivity : ComponentActivity() {

    private val TAG = "TestRealtimeDB"
    private var weightListenerHandle: DistributorObserverRepository.WeightListenerHandle? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        signInAndStartListening()

        setContent {
            SakaTheme {
                Text(text = "Test Realtime Database en cours...")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "critical_channel",
                "Alertes critiques",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showCriticalNotification(distributorId: String, weight: Float) {
        val builder = NotificationCompat.Builder(this, "critical_channel")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Alerte de poids critique")
            .setContentText("Distributeur $distributorId : poids restant = ${weight}g")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun signInAndStartListening() {
        val email = "test@gmail.com"
        val password = "123456"
        val auth = FirebaseAuth.getInstance()
        val repo = RealtimeDatabaseRepository()
        val dataStoreManager = DataStoreManager(applicationContext)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                Log.d(TAG, "✅ Connecté avec $userId")

                lifecycleScope.launch {
                    val distributorId = dataStoreManager.getSelectedDistributor()
                    Log.d(TAG, "Distributor ID récupéré depuis DataStore : $distributorId")
                    if (distributorId.isBlank()) {
                        Log.e(TAG, "❌ Aucun distributeur sélectionné trouvé dans DataStore")
                        return@launch
                    }

                    repo.setCriticalThreshold(distributorId, 200)
                    Log.d(TAG, "🚨 Seuil critique défini à 200g")

                    startWeightObservation(repo, userId, distributorId)

                    runPlanningTests(repo, userId, distributorId)

                    // Test du triggerNow
                    Log.d(TAG, "[TriggerNow] Déclenchement manuel en cours...")
                    repo.triggerNow(userId, distributorId)
                    Log.d(TAG, "[TriggerNow] Champ triggerNow défini à true")
                }


            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Échec de connexion", it)
            }
    }

    private fun startWeightObservation(repo: RealtimeDatabaseRepository, userId: String, distributorId: String) {
        weightListenerHandle = repo.observeCurrentWeight(
            userId = userId,
            distributorId = distributorId,
            onWeightChanged = { currentWeight ->
                Log.d(TAG, "📊 Poids mis à jour : $currentWeight")

                repo.getCriticalThreshold(distributorId) { threshold ->
                    if (threshold != null && currentWeight <= threshold) {
                        Log.w(TAG, "⚠️ Poids critique détecté : $currentWeight ≤ $threshold")
                        showCriticalNotification(distributorId, currentWeight)
                    }
                }
            },
            onError = { error: DatabaseError ->
                Log.e(TAG, "🔥 Erreur Firebase : ${error.message}", error.toException())
            }
        )
    }

    // --- Tests pour la fonctionnalité Interface planning distribution automatique ---
    @RequiresApi(Build.VERSION_CODES.O)
    private fun runPlanningTests(repo: RealtimeDatabaseRepository, userId: String, distributorId: String) {
        Log.d(TAG, "[PlanningAuto] Début des tests planning")

        val planningData = mapOf(
            "time" to "08:00",
            "enabled" to true
        )

        // 1) Créer un nouveau planning (ID auto-généré)
        repo.createPlanning(userId, distributorId, planningData) { success, newPlanningId ->
            if (success && newPlanningId != null) {
                Log.d(TAG, "[PlanningAuto] Planning créé avec ID : $newPlanningId")

                // 2) Mise à jour du planning créé
                val updatedPlanningData = planningData.toMutableMap()
                updatedPlanningData["hour"] = "07:00"
                repo.updatePlanning(userId, distributorId, newPlanningId, updatedPlanningData) { updateSuccess ->
                    if (updateSuccess) {
                        Log.d(TAG, "[PlanningAuto] Planning mis à jour : $newPlanningId")

                        // 3) Suppression du planning
                        repo.deletePlanning(userId, distributorId, newPlanningId) { deleteSuccess ->
                            if (deleteSuccess) {
                                Log.d(TAG, "[PlanningAuto] Planning supprimé : $newPlanningId")
                            } else {
                                Log.e(TAG, "[PlanningAuto] Échec suppression planning : $newPlanningId")
                            }
                        }
                    } else {
                        Log.e(TAG, "[PlanningAuto] Échec mise à jour planning : $newPlanningId")
                    }
                }

            } else {
                Log.e(TAG, "[PlanningAuto] Échec création planning")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weightListenerHandle?.remove()
    }
}
