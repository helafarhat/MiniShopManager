
package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import android.content.Intent
class CartActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView
    private lateinit var tvEmptyCart: TextView

    private val products = ArrayList<PanierProduit>()

    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cart)

        dbHelper = DBHelper(this)

        listView = findViewById(R.id.listViewCart)
        tvTotal = findViewById(R.id.tvTotal)
        tvEmptyCart = findViewById(R.id.tvEmptyCart)

        adapter = CartAdapter()
        listView.adapter = adapter

        val btnCheckout =
            findViewById<Button>(R.id.btnCheckout)


        btnCheckout.setOnClickListener {

            if (products.isEmpty()) {

                Toast.makeText(
                    this,
                    "Votre panier est vide !",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val intent = Intent(
                    this,
                    PaymentActivity::class.java
                )

                startActivity(intent)
            }
        }


        refreshCart()
    }

    private fun refreshCart() {

        products.clear()
        products.addAll(dbHelper.getPanier())

        adapter.notifyDataSetChanged()

        val total = products.sumOf {
            it.prix * it.quantite
        }

        tvTotal.text = "Total : %.2f DT".format(total)

        val isEmpty = products.isEmpty()

        tvEmptyCart.visibility =
            if (isEmpty) View.VISIBLE else View.GONE

        listView.visibility =
            if (isEmpty) View.GONE else View.VISIBLE
    }

    inner class CartAdapter :
        BaseAdapter() {

        override fun getCount(): Int = products.size

        override fun getItem(position: Int): PanierProduit =
            products[position]

        override fun getItemId(position: Int): Long =
            products[position].id.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view = convertView
                ?: LayoutInflater.from(this@CartActivity)
                    .inflate(
                        R.layout.item_cart,
                        parent,
                        false
                    )

            val product = products[position]

            val tvName =
                view.findViewById<TextView>(R.id.tvCartName)

            val tvPrice =
                view.findViewById<TextView>(R.id.tvCartPrice)

            val tvQuantity =
                view.findViewById<TextView>(R.id.tvCartQuantity)

            val btnIncrease =
                view.findViewById<Button>(R.id.btnIncrease)

            val btnDecrease =
                view.findViewById<Button>(R.id.btnDecrease)

            val btnRemove =
                view.findViewById<Button>(R.id.btnRemove)

            tvName.text = product.nom

            tvPrice.text =
                "Prix : %.2f DT".format(product.prix)

            tvQuantity.text = product.quantite.toString()

            btnIncrease.setOnClickListener {
                dbHelper.modifierQuantite(
                    product.id,
                    product.quantite + 1
                )
                refreshCart()
            }

            btnDecrease.setOnClickListener {
                dbHelper.modifierQuantite(
                    product.id,
                    product.quantite - 1
                )
                refreshCart()
            }

            btnRemove.setOnClickListener {
                dbHelper.supprimerProduit(product.id)
                refreshCart()

                Toast.makeText(
                    this@CartActivity,
                    "Produit supprimé",
                    Toast.LENGTH_SHORT
                ).show()
            }

            return view
        }
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized &&
            ::adapter.isInitialized
        ) {
            refreshCart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
