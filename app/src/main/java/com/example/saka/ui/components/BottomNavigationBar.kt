// BottomNavigationBar.kt
package com.example.saka.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val label: String, val icon: ImageVector) {
    object Accueil : BottomNavItem("Accueil", Icons.Default.Home)
    object Planning : BottomNavItem("Planning", Icons.Default.Schedule)
    object Historique : BottomNavItem("Historique", Icons.Default.History)
    object Reglages : BottomNavItem("Réglages", Icons.Default.Settings)
}

@Composable
fun BottomNavigationBar(current: String = "Accueil", onItemSelected: (String) -> Unit = {}) {
    val items = listOf(
        BottomNavItem.Accueil,
        BottomNavItem.Planning,
        BottomNavItem.Historique,
        BottomNavItem.Reglages
    )

    BottomNavigation {
        items.forEach { item ->
            BottomNavigationItem(
                selected = current == item.label,
                onClick = { onItemSelected(item.label) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
