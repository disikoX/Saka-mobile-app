package com.example.saka.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.saka.auth.AuthRepository

@Composable
fun SplashScreen(navController: NavController) {
    val authRepo = AuthRepository()

    LaunchedEffect(Unit) {
        delay(1000) // Pause de 1 seconde (effet splash)

        // Vérifie si un utilisateur est connecté (via Firebase Auth)
        if (authRepo.isUserLoggedIn()) {
            // Si oui, on va directement à la page principale
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Sinon, on redirige vers l’écran de connexion
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
}
