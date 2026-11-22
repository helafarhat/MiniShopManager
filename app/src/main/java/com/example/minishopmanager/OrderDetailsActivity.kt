
package com.example.minishopmanager

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        setupScreen()

        dbHelper = DBHelper(this)

        val container = findViewById<LinearLayout>(R.id.orderProductsContainer)
        val tvOrderNumber = findViewById<TextView>(R.id.tvOrderNumber)
        val tvCustomer = findViewById<TextView>(R.id.tvOrderCustomer)
        val tvDate = findViewById<TextView>(R.id.tvOrderDate)
        val tvPayment = findViewById<TextView>(R.id.tvOrderPayment)
        val tvTotal = findViewById<TextView>(R.id.tvOrderTotal)
        val btnBack = findViewById<Button>(R.id.btnBackOrderDetails)

        val orderId = intent.getIntExtra("ORDER_ID", -1)

        val commande = dbHelper.getCommandes()
            .find { it.id == orderId }

        if (commande != null) {

            tvOrderNumber.text = "Order #${commande.id}"
            tvCustomer.text = "Customer: ${commande.nomClient}"
            tvDate.text = SimpleDateFormat(
                "dd/MM/yyyy - HH:mm",
                Locale.getDefault()
            ).format(Date(commande.date))

            tvPayment.text = "Payment: ${commande.methodePaiement}"
            tvTotal.text = "Total: %.2f DT".format(commande.total)

            val produits = dbHelper.getProduitsCommande(orderId)

            val inflater = LayoutInflater.from(this)

            for (produit in produits) {
                // Row layout lives in res/layout/item_order_product.xml
                val item = inflater.inflate(
                    R.layout.item_order_product,
                    container,
                    false
                )

                item.findViewById<TextView>(R.id.tvOrderProductName).text =
                    "${produit.nom}  x${produit.quantite}"

                item.findViewById<TextView>(R.id.tvOrderProductUnit).text =
                    "%.2f DT each".format(produit.prix)

                container.addView(item)
            }

        } else {
            tvOrderNumber.text = "Order not found"
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
