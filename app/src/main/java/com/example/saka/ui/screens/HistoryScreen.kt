// HistoryScreen.kt
package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.saka.ui.components.BottomNavigationBar

@Composable
fun HistoryScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(current = "Historique",navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Historique des Distributions",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Aujourd'hui", "Cette semaine", "Ce mois").forEach { label ->
                    OutlinedButton(onClick = { /* TODO */ }) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                backgroundColor = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aujourd'hui, 23 Février", fontWeight = FontWeight.Bold)
                    Text("180g distribués", fontSize = 14.sp)
                    Text("3 distributions", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val historyItems = listOf(
                Triple("07:30", 60, true),
                Triple("12:30", 60, true),
                Triple("19:30", 60, true)
            )

            historyItems.forEach { (time, quantity, success) ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(time, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Distribution réussie", fontSize = 12.sp)
                            }
                        }
                        Text("${quantity}g", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
