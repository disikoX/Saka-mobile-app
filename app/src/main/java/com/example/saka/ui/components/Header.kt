package com.example.saka.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Header(
    distributors: List<String>,
    selected: String,
    onDistributorSelected: (String) -> Unit,
    onAddDistributorClick: (String) -> Unit // Callback utilisé pour ajouter un distributeur dans Firestore
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddField by remember { mutableStateOf(false) }
    var newDistributor by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (selected.isBlank()) "Sélectionner un distributeur" else selected,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Menu déroulant"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                showAddField = false
                newDistributor = ""
            }
        ) {
            // Liste des distributeurs liés à l'utilisateur (récupérés depuis Firestore)
            distributors.forEach { distributor ->
                DropdownMenuItem(
                    text = { Text(distributor) },
                    onClick = {
                        onDistributorSelected(distributor)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            if (!showAddField) {
                DropdownMenuItem(
                    text = { Text("Ajouter un distributeur") },
                    onClick = {
                        showAddField = true
                    }
                )
            } else {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = newDistributor,
                        onValueChange = { newDistributor = it },
                        label = { Text("ID du distributeur") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newDistributor.isNotBlank()) {
                                // Déclenche le traitement d’ajout d’un distributeur (appel backend dans l’écran parent)
                                onAddDistributorClick(newDistributor)
                                expanded = false
                                showAddField = false
                                newDistributor = ""
                            }
                        }
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}
