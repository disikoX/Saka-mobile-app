package com.example.saka.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
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
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import java.util.*

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun PlanningScreen() {
    val scrollState = rememberScrollState()
    val distributions = remember { mutableStateListOf("07:00", "12:00") }

    // État des switches par ligne, synchronisé avec distributions
    val switchesState = remember { mutableStateListOf<Boolean>().apply { repeat(distributions.size) { add(true) } } }

    // Synchronisation taille switchesState / distributions
    LaunchedEffect(distributions.size) {
        if (switchesState.size < distributions.size) {
            repeat(distributions.size - switchesState.size) {
                switchesState.add(true)
            }
        } else if (switchesState.size > distributions.size) {
            repeat(switchesState.size - distributions.size) {
                switchesState.removeLast()
            }
        }
    }

    // État pour gérer la translation X des lignes dragables
    val dragOffsetsX = remember { mutableStateListOf<Float>().apply { repeat(distributions.size) { add(0f) } } }

    // Synchronisation taille dragOffsetsX / distributions
    LaunchedEffect(distributions.size) {
        if (dragOffsetsX.size < distributions.size) {
            repeat(distributions.size - dragOffsetsX.size) {
                dragOffsetsX.add(0f)
            }
        } else if (dragOffsetsX.size > distributions.size) {
            repeat(dragOffsetsX.size - distributions.size) {
                dragOffsetsX.removeLast()
            }
        }
    }

    val context = LocalContext.current

    Scaffold(
        bottomBar = { BottomNavigationBar(current = "Planning") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                // Quand on clique quelque part dans la colonne, on remet tous les dragOffsets à 0
                .clickable {
                    for (i in dragOffsetsX.indices) {
                        dragOffsetsX[i] = 0f
                    }
                }
        ) {
            Text("Planning de Distribution", style = MaterialTheme.typography.h6)

            Card(
                backgroundColor = Color(0xFFFFEBEE),
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
                        Text("\uD83D\uDFE2 Connecté", fontSize = 14.sp)
                        Text("75% restant", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Prochaine distribution :", fontWeight = FontWeight.Bold)
                    Text("Aujourd'hui à 18:00")
                }
            }

            Card(
                backgroundColor = Color(0xFFFFEBEE),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\u23F8 Pause Temporaire", fontWeight = FontWeight.Bold)
                    Text("Suspendre temporairement toutes les distributions")
                    Spacer(modifier = Modifier.height(8.dp))

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
                                            jours = ((jours.toIntOrNull() ?: 0) + extraDays).toString()
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
                                // TODO : utiliser jours et heures convertis
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }

            Text("Distributions Aujourd'hui", fontWeight = FontWeight.Bold)

            distributions.forEachIndexed { index, time ->
                var displayTime by remember { mutableStateOf(time) }

                val dragThreshold = -30f // seuil pour montrer la poubelle

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 4.dp)
                ) {
                    // Icône de poubelle en arrière-plan (fixe)
                    if (dragOffsetsX[index] < dragThreshold) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 24.dp)
                                .clickable {
                                    // Supprime la distribution et ses états associés
                                    distributions.removeAt(index)
                                    switchesState.removeAt(index)
                                    dragOffsetsX.removeAt(index)
                                }
                        )
                    }

                    // Ligne draggable
                    Card(
                        modifier = Modifier
                            .offset(x = dragOffsetsX[index].dp)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    dragOffsetsX[index] = (dragOffsetsX[index] + dragAmount).coerceIn(-100f, 0f)
                                }
                            }
                            .clickable {
                                // Remet tous les dragOffsets à zéro quand on clique sur une ligne
                                for (i in dragOffsetsX.indices) {
                                    dragOffsetsX[i] = 0f
                                }
                                // Ouvre le TimePicker pour cette distribution
                                val cal = Calendar.getInstance()
                                val hour = displayTime.split(":").getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY)
                                val minute = displayTime.split(":").getOrNull(1)?.toIntOrNull() ?: cal.get(Calendar.MINUTE)

                                TimePickerDialog(
                                    context,
                                    { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                                        val formattedTime = "%02d:%02d".format(selectedHour, selectedMinute)
                                        distributions[index] = formattedTime
                                        displayTime = formattedTime
                                    },
                                    hour,
                                    minute,
                                    true
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Heure",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = displayTime, fontSize = 18.sp)
                            }

                            if (index == 0) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Distribué",
                                    tint = MaterialTheme.colors.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Switch(
                                    checked = switchesState[index],
                                    onCheckedChange = { isChecked ->
                                        switchesState[index] = isChecked
                                        // TODO : gérer changement état distribution ici
                                    },
                                    modifier = Modifier.size(40.dp, 40.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        distributions.add("00:00")
                        switchesState.add(true)
                        dragOffsetsX.add(0f)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter une distribution",
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}
