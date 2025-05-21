package com.example.saka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saka.ui.theme.SakaTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("messages").document("firstMessage")
        val data = hashMapOf("text" to "Hello from Kotlin!")

        val messageText = mutableStateOf("Loading...")

        docRef.set(data)
            .addOnSuccessListener { println("Document written") }
            .addOnFailureListener { e -> println("Error: $e") }

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val text = document.getString("text") ?: "No text"
                    messageText.value = text
                } else {
                    messageText.value = "No such document"
                }
            }
            .addOnFailureListener { exception ->
                messageText.value = "Error: ${exception.message}"
            }

        enableEdgeToEdge()
        setContent {
            SakaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                    Text(
                        text = messageText.value,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SakaTheme {
        Greeting("Android")
    }
}