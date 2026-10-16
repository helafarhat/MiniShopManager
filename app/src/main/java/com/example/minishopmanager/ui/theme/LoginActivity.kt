
package com.example.minishopmanager

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etStudentName = findViewById<EditText>(R.id.etStudentName)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        prefs = getSharedPreferences("MiniShopPrefs", MODE_PRIVATE)

        val remember = prefs.getBoolean("REMEMBER", false)

        if (remember) {
            etStudentName.setText(prefs.getString("STUDENT", ""))
            cbRemember.isChecked = true
        }

        btnLogin.setOnClickListener {
            val studentName = etStudentName.text.toString().trim()

            if (studentName.isEmpty()) {
                etStudentName.error = "Veuillez saisir votre nom"
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

            Toast.makeText(
                this,
                "Bienvenue $studentName !",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("STUDENT_NAME", studentName)
            startActivity(intent)
            finish()
        }
    }
}
