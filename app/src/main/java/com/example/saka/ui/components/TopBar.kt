package com.example.saka.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saka.auth.AuthRepository
import com.example.saka.local.DataStoreManager

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    val authRepo = AuthRepository()
    val localContext = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(localContext) }
    var selectedDistributor by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        selectedDistributor = dataStoreManager.getSelectedDistributor()
    }

    TopAppBar(
        title = {
            Column(
                modifier = Modifier.padding(start = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Saka",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary // Rouge brique de votre palette
                    )
                )
                Row() {
                    Text(
                        text = "distributeur: ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface // Brun foncé de votre palette
                        )
                    )
                    Text(
                        text = selectedDistributor,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface, // Beige clair de votre palette
            titleContentColor = Color.Black
        ),
        actions = {
            // Icône de déconnexion
            IconButton(
                onClick = {
                    authRepo.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Déconnexion",
                    tint = Color(0xFFBF4C38) // Utilisation de votre couleur rouge brique
                )
            }
        }
    )
}