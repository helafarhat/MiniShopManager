
package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity

class PaymentActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment)

        dbHelper = DBHelper(this)

        val etName = findViewById<EditText>(R.id.etCustomerName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)

        val rgPayment = findViewById<RadioGroup>(R.id.rgPayment)

        val tvTotal = findViewById<TextView>(R.id.tvPaymentTotal)

        val btnConfirm =
            findViewById<Button>(R.id.btnConfirmOrder)

        val products = dbHelper.getPanier()

        val total = products.sumOf {
            it.prix * it.quantite
        }

        tvTotal.text = "%.2f DT".format(total)

        btnConfirm.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Enter your name"
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.error = "Enter your phone number"
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                etAddress.error = "Enter your address"
                return@setOnClickListener
            }

            if (products.isEmpty()) {
                Toast.makeText(
                    this,
                    "Your cart is empty!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val paymentMethod =
                if (rgPayment.checkedRadioButtonId == R.id.rbCard) {
                    "Bank card (demo)"
                } else {
                    "Cash on delivery"
                }

            // Clear the cart after confirming the demo order.
            dbHelper.viderPanier()

            val intent = Intent(
                this,
                OrderConfirmationActivity::class.java
            )

            intent.putExtra("CUSTOMER_NAME", name)
            intent.putExtra("ORDER_TOTAL", total)
            intent.putExtra("PAYMENT_METHOD", paymentMethod)

            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}
