package com.example.saka.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saka.ui.components.BottomNavigationBar
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import java.util.*
import com.example.saka.local.DataStoreManager
import com.example.saka.ui.components.TopBar
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PlanningScreen(navController: NavController) {
    val authRepo = AuthRepository()
    val realtimeRepo = RealtimeDatabaseRepository()
    val scrollState = rememberScrollState()
    val distributions = remember { mutableStateListOf<String>() }
    val switchesState = remember { mutableStateListOf<Boolean>() }
    val dragOffsetsX = remember { mutableStateListOf<Float>() }
    val planningIds = mutableListOf<String>()

    val localContext = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(localContext) }
    val status = remember { mutableStateOf("...") }
    val scope = rememberCoroutineScope()
    val nextDistributionTime = remember { mutableStateOf("...") }
    val isBreakActive = remember { mutableStateOf(false) }
    val breakDuration = remember { mutableStateOf(0) }
    val breakResumeTime = remember { mutableStateOf("...") }

    fun refreshPlannings(userId: String, distributorId: String) {
        realtimeRepo.getPlannings(userId, distributorId) { plannings ->
            distributions.clear()
            switchesState.clear()
            dragOffsetsX.clear()
            planningIds.clear()

            plannings.forEach { (planningId, planningData) ->
                val time = planningData["time"] as? String ?: "00:00"
                val enabled = planningData["enabled"] as? Boolean ?: false

                distributions.add(time)
                switchesState.add(enabled)
                dragOffsetsX.add(0f)
                planningIds.add(planningId)
            }
        }
    }

    fun refreshNewDistributionTime(userId: String, distributorId: String){
        realtimeRepo.getNextDistributionTime(userId, distributorId) { time ->
            Log.d("PlanningScreen", "Next Distribution Time: $time")
            if (time != null) {
                nextDistributionTime.value = time
            }
        }
    }


    LaunchedEffect(Unit) {
        val distributorId = dataStoreManager.getSelectedDistributor()
        val userId = authRepo.getCurrentUserId()
        Log.d("PlanningScreen", "Distributor ID: $distributorId")

        if (distributorId.isNotEmpty()) {
            realtimeRepo.getDistributorStatus(distributorId) { result ->
                Log.d("PlanningScreen", "Status Firebase result: $result")
                status.value = if (result == "Connecté") "🟢 Connecté" else "🔴 Déconnecté"
            }

            if (userId != null) {
                refreshNewDistributionTime(userId, distributorId)
            }
        }

        if (userId != null && distributorId.isNotEmpty()) {
            realtimeRepo.getBreakInfos(userId, distributorId) { duration, active ->
                if (active != null) {
                    isBreakActive.value = active
                }
                if (duration != null) {
                    breakDuration.value = duration
                }

                if (duration != null) {
                    if (active == true && duration > 0) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.HOUR_OF_DAY, duration)
                        val hours = calendar.get(Calendar.HOUR_OF_DAY)
                        val minutes = calendar.get(Calendar.MINUTE)
                        breakResumeTime.value = String.format("%02d:%02d", hours, minutes)
                    }
                }
            }
        }

        if (userId != null && distributorId.isNotEmpty()) {
            refreshPlannings(userId, distributorId)
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomNavigationBar(current = "Planning", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable {
                    for (i in dragOffsetsX.indices) {
                        dragOffsetsX[i] = 0f
                    }
                }
        ) {
            Text("Planning de Distribution", fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(status.value, fontSize = 14.sp)
                        Text("75% restant", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Prochaine distribution :", fontWeight = FontWeight.Bold)
                    Text(nextDistributionTime.value)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u23F8 Pause Temporaire", fontWeight = FontWeight.Bold)
                    Text("Suspendre temporairement toutes les distributions")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isBreakActive.value) {
                        Text("La pause est active.")
                        Text(
                            "Reprise prévue à : ${breakResumeTime.value}",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    val userId = authRepo.getCurrentUserId()
                                    val distributorId = dataStoreManager.getSelectedDistributor()

                                    if (userId != null && distributorId.isNotEmpty()) {
                                        realtimeRepo.configureBreak(
                                            userId = userId,
                                            distributorId = distributorId,
                                            duration = 0,
                                            active = false
                                        ) { success ->
                                            Log.d("PlanningScreen", "Pause désactivée: $success")
                                            if (success) {
                                                isBreakActive.value = false
                                                breakResumeTime.value = "..."
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Reprendre maintenant")
                        }
                    } else {
                        var jours by remember { mutableStateOf("") }
                        var heures by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                OutlinedTextField(
                                    value = jours,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            jours = newValue
                                        }
                                    },
                                    placeholder = { Text("Jours") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("j", modifier = Modifier.padding(end = 4.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                OutlinedTextField(
                                    value = heures,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() }) {
                                            val h = newValue.toIntOrNull() ?: 0
                                            if (h >= 24) {
                                                val extraDays = h / 24
                                                val remainingHours = h % 24
                                                jours = ((jours.toIntOrNull()
                                                    ?: 0) + extraDays).toString()
                                                heures = remainingHours.toString()
                                            } else {
                                                heures = newValue
                                            }
                                        }
                                    },
                                    placeholder = { Text("Heures") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("h", modifier = Modifier.padding(end = 4.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        val totalHours =
                                            (jours.toIntOrNull() ?: 0) * 24 + (heures.toIntOrNull()
                                                ?: 0)
                                        val userId = authRepo.getCurrentUserId()
                                        val distributorId =
                                            dataStoreManager.getSelectedDistributor()

                                        if (userId != null && distributorId.isNotEmpty()) {
                                            realtimeRepo.configureBreak(
                                                userId = userId,
                                                distributorId = distributorId,
                                                duration = totalHours,
                                                active = true
                                            ) { success ->
                                                if (success) {
                                                    // Mise à jour immédiate de l'état
                                                    isBreakActive.value = true
                                                    // Calcul du temps de reprise
                                                    val calendar = Calendar.getInstance()
                                                    calendar.add(Calendar.HOUR_OF_DAY, totalHours)
                                                    val hours = calendar.get(Calendar.HOUR_OF_DAY)
                                                    val minutes = calendar.get(Calendar.MINUTE)
                                                    breakResumeTime.value =
                                                        String.format("%02d:%02d", hours, minutes)
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }

            Text("Distributions Aujourd'hui", fontWeight = FontWeight.Bold)

            distributions.forEachIndexed { index, time ->
                var displayTime by remember { mutableStateOf(time) }


                val maxOffset = -90f // déplacement maximal vers la gauche

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Icône de suppression visible derrière la card
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.Red,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 24.dp)
                            .clickable {
                                scope.launch {
                                    val userId = authRepo.getCurrentUserId()
                                    val distributorId = dataStoreManager.getSelectedDistributor()
                                    if (userId != null && distributorId.isNotEmpty()) {
                                        val planningId = planningIds[index]
                                        realtimeRepo.deletePlanning(
                                            userId, distributorId, planningId
                                        ) { success ->
                                            Log.d("PlanningScreen", "Delete planning $index: $success")
                                            if (success) {
                                                distributions.removeAt(index)
                                                dragOffsetsX.removeAt(index)
                                                switchesState.removeAt(index)
                                            }
                                        }
                                    } else {
                                        // Fallback local
                                        distributions.removeAt(index)
                                        dragOffsetsX.removeAt(index)
                                        switchesState.removeAt(index)
                                    }
                                }
                            }
                    )

                    // Card glissante par-dessus
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .offset(x = dragOffsetsX[index].coerceIn(maxOffset, 0f).dp)
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, _ ->
                                    dragOffsetsX[index] = maxOffset
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = displayTime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val calendar = Calendar.getInstance()
                                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                        val minute = calendar.get(Calendar.MINUTE)

                                        val timePicker = TimePickerDialog(
                                            context,
                                            { _: TimePicker, h: Int, m: Int ->
                                                displayTime = String.format("%02d:%02d", h, m)
                                                val planningId = planningIds[index]
                                                val checked = switchesState[index]

                                                scope.launch {
                                                    val userId = authRepo.getCurrentUserId()
                                                    val distributorId = dataStoreManager.getSelectedDistributor()
                                                    if (userId != null && distributorId.isNotEmpty()) {
                                                        val updatedPlanning = mapOf(
                                                            "time" to displayTime,
                                                            "enabled" to checked
                                                        )
                                                        realtimeRepo.updatePlanning(
                                                            userId, distributorId, planningId, updatedPlanning
                                                        ) { success ->
                                                            Log.d("PlanningScreen", "Update planning $planningId: $success")
                                                            if (success) {
                                                                refreshNewDistributionTime(userId, distributorId)
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            hour, minute, true
                                        )

                                        timePicker.show()
                                    }
                            )

                            Switch(
                                checked = switchesState[index],
                                onCheckedChange = { checked ->
                                    switchesState[index] = checked
                                    val planningId = planningIds[index]

                                    scope.launch {
                                        val userId = authRepo.getCurrentUserId()
                                        val distributorId = dataStoreManager.getSelectedDistributor()
                                        if (userId != null && distributorId.isNotEmpty()) {
                                            val updateData = mapOf(
                                                "time" to displayTime,
                                                "enabled" to checked
                                            )
                                            Log.d("DEBUG_SWITCHES", "Données à mettre à jour (updateData) : $updateData")

                                            realtimeRepo.updatePlanning(
                                                userId, distributorId, planningId, updateData
                                            ) { success ->
                                                Log.d("PlanningScreen", "Update switch $index: $success")
                                                if (success) {
                                                    refreshNewDistributionTime(userId, distributorId)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        // 1. Ajouter la distribution localement
                        distributions.add("00:00")
                        switchesState.add(true)
                        dragOffsetsX.add(0f)

                        scope.launch{
                            // 2. Calculer l'index de la nouvelle distribution (dernière position)
                            val newIndex = distributions.size - 1

                            // 3. Créer la map de mise à jour pour Firebase
                            val updateData = mapOf(
                                "time" to "00:00",
                                "enabled" to true
                            )

                            val userId = authRepo.getCurrentUserId()
                            val distributorId = dataStoreManager.getSelectedDistributor()

                            // 4. Appeler la méthode d'update
                            if (userId != null) {
                                realtimeRepo.createPlanning(
                                    userId,
                                    distributorId,
                                    updateData
                                ) { success, planningId ->
                                    Log.d("PlanningScreen", "Ajout distribution $newIndex : $success - ID: $planningId")
                                    if (success) {
                                        refreshPlannings(userId, distributorId)
                                        refreshNewDistributionTime(userId, distributorId)
                                    }

                                }
                            }
                        }


                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter une distribution",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

        }
    }
}
