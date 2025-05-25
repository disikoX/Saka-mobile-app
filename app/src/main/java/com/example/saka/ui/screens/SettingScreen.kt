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
import com.example.saka.backend.FirestoreRepository
import com.example.saka.ui.components.Header
import com.example.saka.ui.components.SidebarMenu
import com.example.saka.local.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val authRepo = AuthRepository() // Gère l'authentification utilisateur (Firebase Auth)
    val firestoreRepo = FirestoreRepository() // Gère les interactions avec Firestore (quantité, assignation, récupération)
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) } // Gère les préférences locales (distributeur sélectionné)

    val userId = authRepo.getCurrentUserId() // Récupère l'ID de l'utilisateur connecté

    var userDistributors by remember { mutableStateOf(listOf<String>()) } // Liste des distributeurs associés à l'utilisateur
    var selectedDistributor by remember { mutableStateOf("") } // Distributeur actuellement sélectionné
    var quantityInput by remember { mutableStateOf("") } // Quantité entrée par l'utilisateur
    var quantityCurrent by remember { mutableStateOf<Int?>(null) } // Quantité actuelle stockée dans Firestore

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🔁 Lors du chargement de l'écran ou si l'userId change :
    // 🔄 1. On récupère les distributeurs associés à l'utilisateur depuis Firestore
    // 🔄 2. On charge le dernier distributeur sélectionné sauvegardé localement (DataStore)
    LaunchedEffect(userId) {
        if (userId != null) {
            firestoreRepo.getUserDistributors(userId) { distributors ->
                userDistributors = distributors
                scope.launch {
                    val saved = dataStoreManager.getSelectedDistributor()
                    selectedDistributor = if (saved in distributors) saved else distributors.firstOrNull().orEmpty()
                }
            }
        }
    }

    // 🔄 Lorsqu’un nouveau distributeur est sélectionné :
    // 👉 On récupère la quantité actuelle dans Firestore pour mise à jour de l’affichage
    LaunchedEffect(selectedDistributor) {
        if (selectedDistributor.isNotBlank()) {
            firestoreRepo.getQuantity(selectedDistributor) { quantity ->
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
                        authRepo.signOut() // Déconnexion via Firebase Auth
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
                                // 💾 Mise à jour du distributeur sélectionné localement (DataStore)
                                selectedDistributor = newSelected
                                scope.launch {
                                    dataStoreManager.saveSelectedDistributor(newSelected)
                                }
                            },
                            onAddDistributorClick = { newDistributor ->
                                // ➕ Ajout d’un distributeur à la base Firestore
                                // 🔒 On vérifie si le distributeur existe et est bien attribuable
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
                                    firestoreRepo.assignDistributorToUser(userId, newDistributor) { success ->
                                        if (success) {
                                            // 🔄 Mise à jour de l'interface avec le nouveau distributeur
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
                            // ✅ Autoriser uniquement les chiffres dans le champ
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
                                // ✅ Mise à jour de la quantité dans Firestore
                                firestoreRepo.setQuantity(selectedDistributor, quantity)
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
