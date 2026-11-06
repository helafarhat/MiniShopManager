
package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.json.JSONObject

data class Produit(
    val nom: String,
    val prix: Double
)

class CatalogueActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper

    // Your existing product photographs
    private val images = intArrayOf(
        R.drawable.product_phone,
        R.drawable.product_headphones,
        R.drawable.product_watch,
        R.drawable.product_charger,
        R.drawable.product_accessories,
        R.drawable.product_cosmetics
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_catalogue)

        dbHelper = DBHelper(this)

        val gridView = findViewById<GridView>(
            R.id.listViewProduits
        )

        val btnVoirPanier = findViewById<Button>(
            R.id.btnVoirPanier
        )

        chargerProduits(gridView)

        // Open shopping cart
        btnVoirPanier.setOnClickListener {
            val intent = Intent(
                this,
                CartActivity::class.java
            )
            startActivity(intent)
        }
    }

    private fun chargerProduits(gridView: GridView) {

        try {
            val jsonText = assets.open("produits.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonText)
            val jsonArray = jsonObject.getJSONArray("produits")

            val products = mutableListOf<Produit>()

            for (i in 0 until jsonArray.length()) {

                val item = jsonArray.getJSONObject(i)

                products.add(
                    Produit(
                        nom = item.getString("nom"),
                        prix = item.getDouble("prix")
                    )
                )
            }

            gridView.adapter = ProductAdapter(products)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Erreur de chargement : ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    inner class ProductAdapter(
        private val products: List<Produit>
    ) : ArrayAdapter<Produit>(
        this@CatalogueActivity,
        R.layout.item_product_grid,
        products
    ) {

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view = convertView ?: LayoutInflater
                .from(this@CatalogueActivity)
                .inflate(
                    R.layout.item_product_grid,
                    parent,
                    false
                )

            val imageView = view.findViewById<ImageView>(
                R.id.imgProduct
            )

            val textView = view.findViewById<TextView>(
                R.id.tvProductName
            )

            val btnAjouter = view.findViewById<Button>(
                R.id.btnAjouterPanier
            )

            val product = products[position]

            // Product name and price
            textView.text =
                "${product.nom}\n${"%.2f".format(product.prix)} DT"

            // Display the corresponding photograph
            imageView.setImageResource(images[position])
            // Open product details when clicking the product
            view.setOnClickListener {

                val intent = Intent(
                    this@CatalogueActivity,
                    ProductDetailsActivity::class.java
                )

                intent.putExtra("product_name", product.nom)
                intent.putExtra("product_price", product.prix)
                intent.putExtra(
                    "product_image",
                    images[position % images.size]
                )

                startActivity(intent)
            }

            // Add product to SQLite cart
            btnAjouter.setOnClickListener {

                val result = dbHelper.ajouterAuPanier(
                    product.nom,
                    product.prix
                )

                if (result != -1L) {

                    Toast.makeText(
                        this@CatalogueActivity,
                        "${product.nom} ajouté au panier !",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this@CatalogueActivity,
                        "Erreur lors de l'ajout au panier.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            return view
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
