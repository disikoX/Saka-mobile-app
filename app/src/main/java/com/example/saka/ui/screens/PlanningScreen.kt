package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saka.ui.components.BottomNavigationBar

@Composable
fun PlanningScreen() {
    Scaffold(
        bottomBar = { BottomNavigationBar(current = "Planning") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Planning de Distribution", style = MaterialTheme.typography.h6)

            Card(
                backgroundColor = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\uD83D\uDFE2 Connecté", fontSize = 14.sp)
                    Text("75% restant", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Prochaine distribution :", fontWeight = FontWeight.Bold)
                    Text("Aujourd'hui à 18:00")
                }
            }

            Text("Distributions Aujourd'hui", fontWeight = FontWeight.Bold)
            listOf(
                "07:00" to "60g",
                "12:00" to "45g",
                "18:00" to "60g"
            ).forEach { (time, qty) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = time)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = qty)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Plannings", fontWeight = FontWeight.Bold)
                Button(onClick = { /* TODO */ }) {
                    Text("+ Nouveau Planning")
                }
            }

            listOf(
                "Semaine" to "Lun-Ven\n3 distributions",
                "Weekend" to "Sam-Dim\n2 distributions"
            ).forEach { (title, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, fontWeight = FontWeight.Bold)
                            Text(desc, fontSize = 12.sp)
                        }
                        Switch(checked = true, onCheckedChange = { })
                    }
                }
            }

            Card(
                backgroundColor = Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u23F8 Pause Temporaire", fontWeight = FontWeight.Bold)
                    Text("Suspendre temporairement toutes les distributions")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("30 minutes", "1 heure", "2 heures", "4 heures").forEach { label ->
                            OutlinedButton(onClick = { }) {
                                Text(label)
                            }
                        }
                    }
                }
            }
        }
    }
}
