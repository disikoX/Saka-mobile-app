package com.example.saka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.saka.ui.components.SidebarMenu
import com.example.saka.local.DataStoreManager
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

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔁 Chargement des distributeurs et sélection précédente
    LaunchedEffect(userId) {
        if (userId != null) {
            realtimeRepo.getUserDistributors(userId) { distributors ->
                userDistributors = distributors
                scope.launch {
                    val saved = dataStoreManager.getSelectedDistributor()
                    selectedDistributor = if (saved in distributors) saved else distributors.firstOrNull().orEmpty()
                }
            }
        }
    }

    // 🔄 Quand on change de distributeur
    LaunchedEffect(selectedDistributor) {
        if (selectedDistributor.isNotBlank()) {
            realtimeRepo.getQuantity(selectedDistributor) { quantity ->
                quantityCurrent = quantity
                quantityInput = ""
            }
        } else {
            quantityCurrent = null
            quantityInput = ""
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(color = Color(0xFFE3F2FD), modifier = Modifier.fillMaxHeight()) {
                SidebarMenu(
                    currentScreen = "setting",
                    onNavigateToScreen = { route ->
                        scope.launch {
                            drawerState.close()
                            if (route != "home") {
                                navController.navigate(route) {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onLogoutClick = {
                        authRepo.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onCloseClick = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Header(
                            distributors = userDistributors,
                            selected = selectedDistributor,
                            onDistributorSelected = { newSelected ->
                                selectedDistributor = newSelected
                                scope.launch {
                                    dataStoreManager.saveSelectedDistributor(newSelected)
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
                                    realtimeRepo.assignDistributorToUser(userId, newDistributor) { success ->
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
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Distributeur sélectionné : $selectedDistributor")

                    Spacer(modifier = Modifier.height(16.dp))

                    quantityCurrent?.let {
                        Text(text = "Quantité actuelle : $it")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                quantityInput = newValue
                            }
                        },
                        label = { Text("Quantité (à définir)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Valider")
                    }
                }
            }
        }
    }
}
