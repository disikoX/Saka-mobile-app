package com.example.saka.ui.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.saka.auth.AuthRepository

@Composable
fun SplashScreen(navController: NavController) {
    val authRepo = AuthRepository()

    LaunchedEffect(Unit) {
        delay(1000) // short pause of 1 second
        if (authRepo.isUserLoggedIn()) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
}
