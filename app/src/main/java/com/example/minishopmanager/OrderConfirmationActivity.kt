
package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class OrderConfirmationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_confirmation)

        val name = intent.getStringExtra("CUSTOMER_NAME") ?: "Customer"
        val total = intent.getDoubleExtra("ORDER_TOTAL", 0.0)
        val method = intent.getStringExtra("PAYMENT_METHOD") ?: "Cash on delivery"

        val message =
            findViewById<TextView>(R.id.tvConfirmationMessage)

        val btnHome =
            findViewById<Button>(R.id.btnBackHome)

        message.text = """
            Thank you, $name!

            Your order has been recorded.

            Total: %.2f DT

            Payment: $method

            We hope to see you again!
        """.trimIndent().format(total)

        btnHome.setOnClickListener {

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            finish()
        }
    }
}
