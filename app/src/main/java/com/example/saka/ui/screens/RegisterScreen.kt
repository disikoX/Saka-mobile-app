package com.example.saka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository  // Remplace FirestoreRepository

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authRepo = AuthRepository()
    val dbRepo = RealtimeDatabaseRepository() // Nouvelle instance pour Realtime Database

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Inscription", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                authRepo.registerUser(email, password) { success, error ->
                    if (success) {
                        val userId = authRepo.getCurrentUserId()
                        if (userId != null) {
                            // On crée une entrée utilisateur dans Realtime Database
                            dbRepo.createUserDocument(userId, mapOf("email" to email))
                        }
                        onRegisterSuccess()
                    } else {
                        errorMessage = error
                    }
                }
            }) {
                Text("Créer un compte")
            }

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Déjà inscrit ? Se connecter")
            }
        }
    }
}
