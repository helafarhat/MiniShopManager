package com.example.minishopmanager

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ProductDetailsActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        dbHelper = DBHelper(this)

        val imgProduct = findViewById<ImageView>(R.id.imgDetailProduct)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvDescription =
            findViewById<TextView>(R.id.tvDetailDescription)

        val btnAdd = findViewById<Button>(R.id.btnAddDetailCart)
        val btnBack = findViewById<Button>(R.id.btnBackCatalogue)

        // Receive product information
        val name = intent.getStringExtra("product_name") ?: "Produit"
        val price = intent.getDoubleExtra("product_price", 0.0)
        val image = intent.getIntExtra("product_image", 0)

        // Display product information
        tvName.text = name
        tvPrice.text = String.format("%.2f DT", price)

        if (image != 0) {
            imgProduct.setImageResource(image)
        }

        // Product descriptions
        tvDescription.text = when (name) {
            "Téléphone" ->
                "Découvrez notre téléphone, idéal pour rester connecté et profiter de vos applications préférées."

            "Casque Bluetooth" ->
                "Profitez de votre musique et de vos appels avec un casque Bluetooth pratique et confortable."

            "Montre connectée" ->
                "Une montre connectée pour suivre votre quotidien et rester informé en toute simplicité."

            "Chargeur USB" ->
                "Un accessoire indispensable pour recharger vos appareils facilement."

            "Accessoires" ->
                "Découvrez notre sélection d'accessoires pour compléter votre quotidien."

            "Cosmétiques" ->
                "Une sélection de produits de beauté pour prendre soin de vous."

            else ->
                "Découvrez ce produit dans notre boutique MiniShop."
        }

        // Add product to cart
        btnAdd.setOnClickListener {

            val result = dbHelper.ajouterAuPanier(name, price)

            if (result != -1L) {
                Toast.makeText(
                    this,
                    "$name ajouté au panier !",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Erreur lors de l'ajout au panier.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Return to catalogue
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