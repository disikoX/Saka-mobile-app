package com.example.saka.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.ui.components.Header
import com.example.saka.local.DataStoreManager
import com.example.saka.ui.components.BottomNavigationBar
import com.example.saka.ui.components.TopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val authRepo = AuthRepository()
    val realtimeRepo = RealtimeDatabaseRepository()
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    val userId = authRepo.getCurrentUserId()

    var userDistributors by remember { mutableStateOf(listOf<String>()) }
    var selectedDistributor by remember { mutableStateOf("") }

    var quantityInput by remember { mutableStateOf("") }
    var quantityCurrent by remember { mutableStateOf<Int?>(null) }

    var criticalThresholdInput by remember { mutableStateOf("") }
    var criticalThresholdCurrent by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        if (userId != null) {
            realtimeRepo.getUserDistributors(userId) { distributors ->
                userDistributors = distributors
                scope.launch {
                    val saved = dataStoreManager.getSelectedDistributor()
                    selectedDistributor =
                        if (saved in distributors) saved else distributors.firstOrNull().orEmpty()
                }
            }
        }
    }

    LaunchedEffect(selectedDistributor) {
        if (selectedDistributor.isNotBlank()) {
            realtimeRepo.getQuantity(selectedDistributor) { quantity ->
                quantityCurrent = quantity
                quantityInput = ""
            }
            realtimeRepo.getCriticalThreshold(selectedDistributor) { threshold ->
                criticalThresholdCurrent = threshold
                criticalThresholdInput = ""
            }
        } else {
            quantityCurrent = null
            quantityInput = ""
            criticalThresholdCurrent = null
            criticalThresholdInput = ""
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(current = "setting", navController) },
        topBar = {
            Column {
                TopBar(navController)
                TopAppBar(
                    title = {
                        Header(
                            distributors = userDistributors,
                            selected = selectedDistributor,
                            onDistributorSelected = { newSelected ->
                                selectedDistributor = newSelected
                                scope.launch {
                                    dataStoreManager.saveSelectedDistributor(newSelected)
                                    val activity = (context as? Activity)
                                    activity?.recreate()
                                }
                            },
                            onAddDistributorClick = { newDistributor ->
                                if (newDistributor.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Veuillez saisir un ID valide")
                                    }
                                    return@Header
                                }
                                if (userDistributors.contains(newDistributor)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Ce distributeur est déjà ajouté.")
                                    }
                                    return@Header
                                }

                                if (userId != null) {
                                    realtimeRepo.assignDistributorToUser(
                                        userId,
                                        newDistributor
                                    ) { success ->
                                        if (success) {
                                            userDistributors = userDistributors + newDistributor
                                            selectedDistributor = newDistributor
                                            scope.launch {
                                                dataStoreManager.saveSelectedDistributor(newDistributor)
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Erreur : Distributeur inexistant ou déjà assigné.")
                                            }
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Erreur : utilisateur non authentifié.")
                                    }
                                }
                            }
                        )
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Spacer(modifier = Modifier.height(16.dp))

                quantityCurrent?.let {
                    Text(text = "Quantité de la ration actuelle (en g) : $it")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            quantityInput = newValue
                        }
                    },
                    label = { Text("Quantité de la ration (à définir en g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    enabled = quantityInput.isNotBlank() && selectedDistributor.isNotBlank(),
                    onClick = {
                        val quantity = quantityInput.toIntOrNull()
                        if (quantity == null || userId == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Valeur invalide ou utilisateur non connecté")
                            }
                        } else if (quantity > 1000) {
                            scope.launch {
                                snackbarHostState.showSnackbar("La quantité maximale autorisée est de 1000g")
                            }
                        } else {
                            realtimeRepo.setQuantity(selectedDistributor, quantity)
                            quantityCurrent = quantity
                            quantityInput = ""
                            scope.launch {
                                snackbarHostState.showSnackbar("Quantité $quantity définie pour $selectedDistributor")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Valider la quantité")
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                criticalThresholdCurrent?.let {
                    Text(text = "Seuil critique actuel (en g) : $it")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = criticalThresholdInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            criticalThresholdInput = newValue
                        }
                    },
                    label = { Text("Seuil critique à définir (en g)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    enabled = criticalThresholdInput.isNotBlank() && selectedDistributor.isNotBlank(),
                    onClick = {
                        val threshold = criticalThresholdInput.toIntOrNull()
                        if (threshold == null || userId == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Valeur seuil invalide ou utilisateur non connecté")
                            }
                        } else if (threshold > 1000) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Le seuil maximal autorisé est de 1000g")
                            }
                        } else {
                            realtimeRepo.setCriticalThreshold(selectedDistributor, threshold)
                            criticalThresholdCurrent = threshold
                            criticalThresholdInput = ""
                            scope.launch {
                                snackbarHostState.showSnackbar("Seuil critique $threshold défini pour $selectedDistributor")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Valider le seuil critique")
                }
            }
        }
    }
}
