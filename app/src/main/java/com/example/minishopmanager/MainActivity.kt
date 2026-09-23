package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.minishopmanager.ui.theme.MiniShopManagerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("LIFECYCLE", "onCreate")

        setContent {
            MiniShopManagerTheme {
                MainScreen(
                    onNextClick = {
                        Toast.makeText(
                            this,
                            "Bonjour Hella Farhat !",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("LIFECYCLE", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LIFECYCLE", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LIFECYCLE", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFECYCLE", "onDestroy")
    }
}

@Composable
fun MainScreen(
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "MiniShopManager")

        Button(
            onClick = onNextClick
        ) {
            Text(text = "Suivant")
        }
    }
}