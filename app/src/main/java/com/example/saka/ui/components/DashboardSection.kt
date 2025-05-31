package com.example.saka.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(Icons.Default.Schedule, "4", "distributions")
                InfoItem(Icons.Default.Scale, "120g", "distribués")
                InfoItem(Icons.Default.DateRange, "2h", "dernière")
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF7D5260),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(32.dp)
                )

                Column {
                    Text("Prochain repas", color = Color(0xFF7D5260), fontWeight = FontWeight.Bold)
                    Text("prévu à 18:00", color = Color(0xFF7D5260))
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
        Icon(icon, contentDescription = null, tint = Color(0xFF7D5260), modifier = Modifier.size(24.dp))
        Text(text = value, fontWeight = FontWeight.Bold, color = Color(0xFF7D5260))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF7D5260))
    }
}