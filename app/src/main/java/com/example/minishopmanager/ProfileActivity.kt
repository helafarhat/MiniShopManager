package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        // The profile belongs to a logged-in user; guests go through the login first
        val studentName = Session.currentUser(this)

        if (studentName == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra(
                        LoginActivity.EXTRA_NEXT,
                        LoginActivity.NEXT_PROFILE
                    )
            )
            finish()
            return
        }

        setContentView(R.layout.activity_profile)
        setupScreen()

        findViewById<TextView>(R.id.tvProfileName).text = studentName

        // Order history (existing screen)
        findViewById<Button>(R.id.btnProfileOrders).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        // Back to guest mode, then return to Home
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Session.logOut(this)

            Toast.makeText(
                this,
                "Vous êtes déconnecté.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}