
package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_order_history)
        setupScreen()

        dbHelper = DBHelper(this)

        val listOrders = findViewById<LinearLayout>(R.id.listOrders)
        val tvEmpty = findViewById<View>(R.id.tvEmptyOrders)
        val btnBack = findViewById<Button>(R.id.btnBackHistory)

        btnBack.setOnClickListener {
            finish()
        }

        afficherCommandes(listOrders, tvEmpty)
    }

    private fun afficherCommandes(
        container: LinearLayout,
        emptyMessage: View
    ) {
        val commandes = dbHelper.getCommandes()

        container.removeAllViews()

        if (commandes.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            return
        }

        emptyMessage.visibility = View.GONE

        val dateFormat = SimpleDateFormat(
            "dd/MM/yyyy - HH:mm",
            Locale.getDefault()
        )

        val inflater = LayoutInflater.from(this)

        for (commande in commandes) {

            // Card layout lives in res/layout/item_order.xml (dp-based spacing)
            val card = inflater.inflate(R.layout.item_order, container, false)

            card.findViewById<TextView>(R.id.tvOrderItemTitle).text =
                "Order #${commande.id}"

            card.findViewById<TextView>(R.id.tvOrderItemDate).text =
                dateFormat.format(Date(commande.date))

            card.findViewById<TextView>(R.id.tvOrderItemCustomer).text =
                "Customer: ${commande.nomClient}"

            card.findViewById<TextView>(R.id.tvOrderItemTotal).text =
                "%.2f DT".format(commande.total)

            val openDetails = View.OnClickListener {
                val intent = Intent(
                    this@OrderHistoryActivity,
                    OrderDetailsActivity::class.java
                )

                intent.putExtra("ORDER_ID", commande.id)
                startActivity(intent)
            }

            // The whole card and the button both open the order details
            card.findViewById<View>(R.id.cardOrder).setOnClickListener(openDetails)
            card.findViewById<Button>(R.id.btnOrderDetails).setOnClickListener(openDetails)

            container.addView(card)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
