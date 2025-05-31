package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saka.ui.components.BottomNavItem

@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Niveau de croquettes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("73%", fontSize = 40.sp, color = Color.Green)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { /* Appel distribution */ }) {
                Icon(Icons.Filled.Pets, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Distribuer maintenant")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aujourd'hui", fontWeight = FontWeight.Bold)
                    Text("4 distributions")
                    Text("120g distribués")
                    Text("Dernière : il y a 2h")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Prochain repas prévu à 18:00", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Connecté | Batterie : 85%", fontSize = 14.sp)
        }
    }
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
