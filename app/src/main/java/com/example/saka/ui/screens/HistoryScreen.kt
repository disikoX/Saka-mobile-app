// HistoryScreen.kt
package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.saka.ui.components.BottomNavigationBar
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.saka.backend.repositories.HistoryEntry
import com.example.saka.ui.components.TopBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.saka.local.DataStoreManager
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import java.util.Calendar

@Composable
fun HistoryScreen(navController: NavController) {
    var selectedPeriod by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val historyList = remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    val localContext = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(localContext) }
    val authRepo = AuthRepository()
    val realtimeRepo = RealtimeDatabaseRepository()

    fun isToday(timeInMillis: Long): Boolean {
        val cal = Calendar.getInstance()
        val today = cal.clone() as Calendar

        cal.timeInMillis = timeInMillis
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    fun isThisWeek(timeInMillis: Long): Boolean {
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)

        cal.timeInMillis = timeInMillis
        val entryWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val entryYear = cal.get(Calendar.YEAR)

        return currentWeek == entryWeek && currentYear == entryYear
    }


    LaunchedEffect(
        Unit
    ) {
        val distributorId = dataStoreManager.getSelectedDistributor()
        val userId = authRepo.getCurrentUserId()

        if (userId != null) {
            realtimeRepo.getHistory(userId, distributorId) { list ->
                historyList.value = list
            }
        }

    }

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomNavigationBar(current = "history", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Périodes de filtre
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                listOf("Aujourd'hui", "Cette semaine").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = {
                            selectedPeriod = if (selectedPeriod == period) null else period
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        label = {
                            Text(
                                period,
                                fontSize = 14.sp,
                                fontWeight = if (period == selectedPeriod) FontWeight.Medium else FontWeight.Normal
                            )
                        })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredList = historyList.value.filter { entry ->
                when (selectedPeriod) {
                    "Aujourd'hui" -> isToday(entry.time)
                    "Cette semaine" -> isThisWeek(entry.time)
                    else -> true
                }
            }.sortedByDescending { it.time }

            filteredList.forEach { entry ->


            val formattedTime = remember(entry.time) {
                    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(entry.time))
                }

                val formattedDate = remember(entry.time) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(entry.time))
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(formattedTime, fontWeight = FontWeight.Bold)

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (entry.success) Icons.Default.Verified else Icons.Default.Error,
                                        contentDescription = if (entry.success) "Distribution réussie" else "Échec de distribution",
                                        tint = if (entry.success) Color.Green else Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (entry.success) "Distribution réussie" else "Échec de distribution",
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Text("${entry.quantity}g", fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}

