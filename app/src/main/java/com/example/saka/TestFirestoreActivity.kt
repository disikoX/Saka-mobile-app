package com.example.saka

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.ui.theme.SakaTheme
import com.google.firebase.auth.FirebaseAuth

class TestRealtimeDatabaseActivity : ComponentActivity() {

    private val TAG = "TestRealtimeDB"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        testRealtimeDatabase()

        setContent {
            SakaTheme {
                Text(text = "Test Realtime Database en cours...")
            }
        }
    }

    private fun testRealtimeDatabase() {
        val email = "test@gmail.com"
        val password = "123456"
        val distributorId = "D123"

        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                Log.d(TAG, "✅ Connecté avec $userId")

                val repo = RealtimeDatabaseRepository()

                // 1. Définir un poids test (ex: 151g)
                repo.setCurrentWeight(distributorId, 151f)
                Log.d(TAG, "📦 Poids défini à 151g")

                // 2. Définir un seuil critique (ex: 150g)
                repo.setCriticalThreshold(distributorId, 150)
                Log.d(TAG, "🚨 Seuil critique défini à 150g")

                // 3. Lire le seuil critique pour vérif
                repo.getCriticalThreshold(distributorId) { seuil ->
                    Log.d(TAG, "📌 Seuil récupéré: $seuil")

                    // 4. Vérifier si le poids est critique
                    repo.checkIfWeightIsCritical(distributorId) { isCritical ->
                        when (isCritical) {
                            null -> Log.e(TAG, "❌ Erreur lors de la vérification du seuil critique")
                            true -> Log.w(TAG, "⚠️ Poids CRITIQUE (≤ seuil)")
                            false -> Log.d(TAG, "✅ Poids OK (> seuil)")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Échec de connexion", it)
            }
    }
}
