
package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Retrieve the student's name
        val studentName = intent.getStringExtra("STUDENT_NAME")

        val tvWelcomeName =
            findViewById<TextView>(R.id.tvWelcomeName)

        if (!studentName.isNullOrBlank()) {
            tvWelcomeName.text =
                "Bienvenue $studentName dans votre boutique !"
        }

        // Open catalogue
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
