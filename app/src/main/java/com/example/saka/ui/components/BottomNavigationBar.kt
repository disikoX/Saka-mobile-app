// BottomNavigationBar.kt
package com.example.saka.ui.components

import android.util.Log
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(val label: String, val icon: ImageVector,val route:String) {
    object Accueil : BottomNavItem("Accueil", Icons.Default.Home,"home")
    object Planning : BottomNavItem("Planning", Icons.Default.Schedule,"planning")
    object Historique : BottomNavItem("Historique", Icons.Default.History,"history")
    object Reglages : BottomNavItem("Réglages", Icons.Default.Settings,"settings")
}

@Composable
fun BottomNavigationBar(current: String = "Accueil",navController: NavController, onItemSelected: (String) -> Unit = {}) {
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
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                onClick = {
                    if (current != item.route) {
                        navController.navigate(item.route)
                    }
                },
            )
        }
    }
}
