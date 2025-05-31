package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.saka.ui.components.BottomNavItem
import com.example.saka.ui.components.BottomNavigationBar
import com.example.saka.ui.components.DashboardSection

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(current = "home", navController) }
    ) {
        Text("Mon Distributeur : distributeur 123", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            //Circular Progress Indicator
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = 0.73f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "73%",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Niveau de croquettes", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { /* Appel distribution */ }) {
                Icon(Icons.Filled.Pets, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Distribuer maintenant")
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardSection()

        }
    }
}