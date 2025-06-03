package com.example.saka.ui.components

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.backend.repositories.SuccessStats
import com.example.saka.local.DataStoreManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardSection() {
    val authRepo = AuthRepository()
    val realtimeRepo = RealtimeDatabaseRepository()
    val localContext = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(localContext) }
    val currentWeight = remember { mutableStateOf("...") }
    val threshold = remember { mutableStateOf<Int?>(null) }
    val capacityValue = remember { mutableStateOf<Int?>(null) }
    val successStats = remember { mutableStateOf<SuccessStats?>(null) }
    val nextDistributionTime = remember { mutableStateOf("...") }

    fun refreshNewDistributionTime(userId: String, distributorId: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            realtimeRepo.getNextDistributionTime(userId, distributorId) { time ->
                Log.d("PlanningScreen", "Next Distribution Time: $time")
                if (time != null) {
                    nextDistributionTime.value = time
                }
            }
        }
    }

    fun formatTimeFromTimestamp(latestTime: Long): String {
        if (latestTime == 0L) return "Aucune"
        val date = Date(latestTime)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

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

    fun isCritical(currentWeight: String, threshold: Int?): Boolean {
        if (threshold == null) return false

        // Extraire la valeur numérique de currentWeight (ex: "123.4 g" -> 123.4)
        val cleanedWeight = currentWeight.filter { it.isDigit() || it == '.' }
        val current = cleanedWeight.toFloatOrNull() ?: return false

        // Retourne true si current <= threshold
        return current <= threshold
    }

    LaunchedEffect(Unit) {
        val distributorId = dataStoreManager.getSelectedDistributor()
        val userId = authRepo.getCurrentUserId()

        if (distributorId.isNotEmpty()) {
            if (userId != null) {
                refreshNewDistributionTime(userId, distributorId)
            }
        }

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

        if (userId != null) {
            realtimeRepo.getSuccessStats(userId, distributorId) { stats ->
                successStats.value = stats
            }
        }


    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        if (isCritical(currentWeight.value, threshold.value)) {
            Text(
                text = "Alerte",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                    )

                    Column {
                        Text("Niveau de croquettes bas", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        val percentage = calculatePercentage(currentWeight.value, capacityValue.value)
                        Row {
                            Text(text = percentage?.let { "$it% " } ?: "... ", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text("restants", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }


        // 🟤 Section "Aujourd'hui"
        Text(
            text = "Aujourd’hui",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(
                    Icons.Default.Schedule,
                    successStats.value?.count?.toString() ?: "...",
                    "distributions"
                )
                InfoItem(
                    Icons.Default.Scale,
                    "${successStats.value?.totalQuantity ?: 0}g",
                    "distribués"
                )
                InfoItem(
                    Icons.Default.DateRange,
                    formatTimeFromTimestamp(successStats.value?.latestTime ?: 0L),
                    "dernière"
                )
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🟠 Section "Planification"
        Text(
            text = "Planification",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(32.dp)
                )

                Column {
                    Text(
                        "Prochain repas",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "prévu à ${nextDistributionTime.value}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Text(text = value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
    }
}