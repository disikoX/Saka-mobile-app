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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(val label: String, val icon: ImageVector,val route:String) {
    object Accueil : BottomNavItem("Accueil", Icons.Default.Home,"home")
    object Planning : BottomNavItem("Planning", Icons.Default.Schedule,"planning")
    object Historique : BottomNavItem("Historique", Icons.Default.History,"history")
    object Reglages : BottomNavItem("Réglages", Icons.Default.Settings,"setting")
}

@Composable
fun BottomNavigationBar(
    current: String = "home", // use route, not label
    navController: NavController
) {
    val items = listOf(
        BottomNavItem.Accueil,
        BottomNavItem.Planning,
        BottomNavItem.Historique,
        BottomNavItem.Reglages
    )

    BottomNavigation(
        backgroundColor = Color(0xFFFFF2F0), // fond rosé
        contentColor = Color.DarkGray
    ) {
        items.forEach { item ->
            val isSelected = current == item.route
            BottomNavigationItem(
                selected = isSelected,
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) Color(0xFF8B4D36) else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) Color(0xFF8B4D36) else Color.Gray
                    )
                },
                onClick = {
                    if (current != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = true,
                selectedContentColor = Color(0xFF8B4D36), // même que l'icône sélectionnée
                unselectedContentColor = Color.Gray
            )
        }
    }
}