package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnVoirCatalogue =
            findViewById<Button>(R.id.btnVoirCatalogue)

        btnVoirCatalogue.setOnClickListener {

            val intent = Intent(
                this,
                CatalogueActivity::class.java
            )

            startActivity(intent)
        }
    }
}