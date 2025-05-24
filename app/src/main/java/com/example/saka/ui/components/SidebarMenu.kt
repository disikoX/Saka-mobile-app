package com.example.saka.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SidebarMenu(
    currentScreen: String,
    onNavigateToScreen: (String) -> Unit,
    onLogoutClick: () -> Unit, // Callback déclenché pour la déconnexion (appel backend dans l'écran parent)
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        "setting" to "Paramètre",
        // tu peux ajouter d'autres écrans ici, par exemple "profile" to "Profil"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Menu", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer"
                    )
                }
            }

            screens.forEach { (route, label) ->
                val isSelected = route == currentScreen
                TextButton(
                    onClick = { onNavigateToScreen(route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = label)
                }
            }
        }

        // Bouton déclenchant la déconnexion de l'utilisateur (via le callback onLogoutClick)
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter", color = Color.White)
        }
    }
}
