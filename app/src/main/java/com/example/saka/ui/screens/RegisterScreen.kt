package com.example.saka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.FirestoreRepository

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,      // Appelé si l'inscription réussit
    onNavigateToLogin: () -> Unit       // Redirige vers l'écran de connexion
) {
    val authRepo = AuthRepository()         // Gère l'inscription via Firebase Auth (backend)
    val firestoreRepo = FirestoreRepository() // Gère les données utilisateur dans Firestore (backend)

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

            // Champ email
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Champ mot de passe
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bouton d'inscription – crée un compte avec Firebase Auth
            Button(onClick = {
                authRepo.registerUser(email, password) { success, error ->
                    if (success) {
                        // Si succès, on récupère l'ID utilisateur courant
                        val userId = authRepo.getCurrentUserId()
                        if (userId != null) {
                            // Création d’un document utilisateur dans Firestore
                            firestoreRepo.createUserDocument(userId, mapOf("email" to email))
                        }
                        onRegisterSuccess()
                    } else {
                        errorMessage = error // En cas d’erreur, on l’affiche
                    }
                }
            }) {
                Text("Créer un compte")
            }

            // Affichage de l’erreur si besoin
            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Redirection vers la connexion
            TextButton(onClick = onNavigateToLogin) {
                Text("Déjà inscrit ? Se connecter")
            }
        }
    }
}
