package com.example.saka.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.saka.auth.AuthRepository

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,          // Callback appelé si connexion OK
    onNavigateToRegister: () -> Unit     // Redirige vers l'écran d'inscription
) {
    val authRepo = AuthRepository()      // Lien avec le backend pour l’authentification

    // États pour les champs de saisie et l’erreur
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
            Text("Connexion", style = MaterialTheme.typography.headlineMedium)
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

            // Bouton de connexion – appelle signIn() du backend
            Button(onClick = {
                authRepo.signIn(email, password) { success, error ->
                    if (success) {
                        onLoginSuccess()    // Succès : on redirige
                    } else {
                        errorMessage = error // Échec : on affiche l'erreur
                    }
                }
            }) {
                Text("Se connecter")
            }

            // Message d'erreur affiché si connexion échouée
            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Redirection vers l’inscription
            TextButton(onClick = onNavigateToRegister) {
                Text("Pas encore inscrit ? S’inscrire")
            }
        }
    }
}
