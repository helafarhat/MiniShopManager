
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
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import android.widget.BaseAdapter
import android.widget.ImageButton

data class Produit(
    val nom: String,
    val prix: Double
)

class CatalogueActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private val images = ProductCatalog.images

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_catalogue)
        setupScreen()

        dbHelper = DBHelper(this)

        val gridView = findViewById<GridView>(
            R.id.listViewProduits
        )

        chargerProduits(gridView)
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

            val priceView = view.findViewById<TextView>(
                R.id.tvProductPrice
            )

            val btnAjouter = view.findViewById<Button>(
                R.id.btnAjouterPanier
            )

            val product = products[position]

            // Product name and price (separate views so the price can be emphasised)
            textView.text = product.nom
            priceView.text = "%.2f DT".format(product.prix)

            // Display the corresponding photograph
            imageView.setImageResource(images[position])
            // Heart: save / remove this product from favorites      <-- ADD
            view.findViewById<ImageButton>(R.id.btnFavorite)         // <-- ADD
                .bindFavorite(dbHelper, product.nom, product.prix)
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
                    this@CatalogueActivity.refreshCartBadge()
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
    override fun onResume() {
        super.onResume()

        // Hearts may have changed on a product page or on the Favorites screen
        (findViewById<GridView>(R.id.listViewProduits).adapter as? BaseAdapter)
            ?.notifyDataSetChanged()
    }
    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
