package com.example.saka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.saka.ui.theme.SakaTheme
import androidx.navigation.compose.rememberNavController
import com.example.saka.ui.navigation.AppNavGraph
//import com.example.saka.ui.screens.HistoryScreen
import com.example.saka.ui.screens.HomeScreen
//import com.example.saka.ui.screens.PlanningScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            /*SakaTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }*/
            HomeScreen()
            //PlanningScreen()
            //HistoryScreen()
        }
    }
}