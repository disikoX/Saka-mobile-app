package com.example.saka.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saka.ui.screens.*

@SuppressLint("NewApi")
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            LoginScreen(
                // En cas de succès du login, redirige vers l'écran principal (setting)
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                // En cas de succès du register, redirige vers l'écran principal (setting)
                onRegisterSuccess = { navController.navigate("home") },
                onNavigateToLogin = { navController.popBackStack("login", false) }
            )
        }

        composable("setting") {
            SettingScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("planning") {
            PlanningScreen(navController)
        }

        composable("history"){
            HistoryScreen(navController)
        }

    }
}
