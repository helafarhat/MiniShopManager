package com.example.minishopmanager

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        /** Where to go after a successful login (see the NEXT_* values). */
        const val EXTRA_NEXT = "NEXT_SCREEN"
        const val NEXT_PAYMENT = "PAYMENT"
        const val NEXT_PROFILE = "PROFILE"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupScreen()

        val etStudentName = findViewById<EditText>(R.id.etStudentName)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSubtitle = findViewById<TextView>(R.id.tvLoginSubtitle)

        val next = intent.getStringExtra(EXTRA_NEXT)

        // Tell the user why they are being asked to log in
        when (next) {
            NEXT_PAYMENT ->
                tvSubtitle.text = "Connectez-vous pour finaliser votre commande."

            NEXT_PROFILE ->
                tvSubtitle.text = "Connectez-vous pour accéder à votre profil."
        }

        prefs = getSharedPreferences("MiniShopPrefs", MODE_PRIVATE)

        val remember = prefs.getBoolean("REMEMBER", false)

        if (remember) {
            etStudentName.setText(prefs.getString("STUDENT", ""))
            cbRemember.isChecked = true
        }

        btnLogin.setOnClickListener {
            val studentName = etStudentName.text.toString().trim()

            if (studentName.isEmpty()) {
                etStudentName.showFieldError("Veuillez saisir votre nom")
                return@setOnClickListener
            }

            if (cbRemember.isChecked) {
                prefs.edit()
                    .putString("STUDENT", studentName)
                    .putBoolean("REMEMBER", true)
                    .apply()
            } else {
                prefs.edit()
                    .remove("STUDENT")
                    .putBoolean("REMEMBER", false)
                    .apply()
            }

            Session.logIn(studentName)

            Toast.makeText(
                this,
                "Bienvenue $studentName !",
                Toast.LENGTH_SHORT
            ).show()

            // Continue where the user was heading instead of starting over.
            // This screen is finished, so Back from the destination returns
            // to the screen that asked for the login (Cart or Home).
            when (next) {
                NEXT_PAYMENT ->
                    startActivity(Intent(this, PaymentActivity::class.java))

                NEXT_PROFILE ->
                    startActivity(Intent(this, ProfileActivity::class.java))
            }

            finish()
        }
    }
}