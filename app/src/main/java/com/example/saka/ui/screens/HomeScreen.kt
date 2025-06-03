package com.example.saka.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.local.DataStoreManager
import com.example.saka.ui.components.BottomNavigationBar
import com.example.saka.ui.components.DashboardSection
import com.example.saka.ui.components.TopBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val authRepo = AuthRepository()
    val realtimeRepo = RealtimeDatabaseRepository()
    val localContext = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(localContext) }
    val currentWeight = remember { mutableStateOf("...") }
    val capacityValue = remember { mutableStateOf<Int?>(null) }
    val threshold = remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun calculatePercentage(currentWeight: String, capacityValue: Int?): Int? {
        // On garde uniquement les chiffres et le point décimal
        val cleanedWeight = currentWeight.filter { it.isDigit() || it == '.' }

        val current = cleanedWeight.toFloatOrNull()
        val capacity = capacityValue?.toFloat()

        return if (current != null && capacity != null && capacity > 0f) {
            ((current / capacity) * 100).toInt()
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        val distributorId = dataStoreManager.getSelectedDistributor()
        val userId = authRepo.getCurrentUserId()

        if (distributorId.isNotEmpty() && !userId.isNullOrEmpty()) {
            realtimeRepo.observeCurrentWeight(
                userId,
                distributorId,
                onWeightChanged = { weight ->
                    currentWeight.value = "${weight} g"
                },
                onError = { error ->
                    currentWeight.value = "Erreur : ${error.message}"
                }
            )
            // Si besoin : stocke handle pour pouvoir le supprimer plus tard
        } else {
            currentWeight.value = "ID utilisateur ou distributeur manquant"
        }

        if (distributorId.isNotEmpty()) {
            realtimeRepo.getCapacity(distributorId) { capacity ->
                capacity?.let {
                    Log.d("GetCapacity", "Capacité récupérée : $it")
                    capacityValue.value = it
                } ?: run {
                    Log.w("GetCapacity", "Capacité nulle pour l'ID : $distributorId")
                }
            }
        }

        realtimeRepo.getCriticalThreshold(distributorId) {
            Log.d("CriticalThreshold", "Seuil critique récupéré : $it")
            threshold.value = it
        }

    }

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomNavigationBar(current = "home", navController) },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { it ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text(
                    text = "Niveau de croquettes: ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentWeight.value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
            // Dans ton Composable
            val percentage = calculatePercentage(currentWeight.value, capacityValue.value)
            val progressValue = percentage?.div(100f) ?: 0f

            // Logs explicites
            Log.d("ProgressDebug", "Valeur actuelle (currentWeight) reçue : ${currentWeight.value}")
            Log.d(
                "ProgressDebug",
                "Capacité maximale (capacityValue) reçue : ${capacityValue.value}"
            )
            Log.d("ProgressDebug", "Pourcentage calculé : $percentage%")
            Log.d("ProgressDebug", "Valeur de progression pour le cercle : $progressValue")

            // Circular Progress Indicator
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = percentage?.let { "$it%" } ?: "...",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                scope.launch {
                    val userId = authRepo.getCurrentUserId()
                    val distributorId = dataStoreManager.getSelectedDistributor()
                    if (userId != null && distributorId.isNotEmpty()) {
                        realtimeRepo.triggerNow(userId, distributorId)
                        snackbarHostState.showSnackbar("Distribution déclenchée")
                    } else {
                        snackbarHostState.showSnackbar("Impossible de déclencher la distribution")
                    }
                }
            }) {
                Icon(Icons.Filled.Pets, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Distribuer maintenant")
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardSection()

        }
    }
}