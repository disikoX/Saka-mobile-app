package com.example.saka.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.saka.auth.AuthRepository
import com.example.saka.backend.RealtimeDatabaseRepository
import com.example.saka.local.DataStoreManager
import com.example.saka.ui.components.BottomNavItem
import com.example.saka.ui.components.BottomNavigationBar
import com.example.saka.ui.components.DashboardSection
import com.example.saka.ui.components.TopBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomNavigationBar(current = "home", navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(){
                Text("Niveau de croquettes: ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("150 g", fontSize = 20.sp, fontWeight = FontWeight.Bold,color = MaterialTheme.colorScheme.primary)
            }


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
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "73%",
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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