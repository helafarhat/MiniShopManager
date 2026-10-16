
package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
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

    private val images = intArrayOf(
        android.R.drawable.ic_menu_call,
        android.R.drawable.ic_btn_speak_now,
        android.R.drawable.ic_menu_recent_history,
        android.R.drawable.ic_menu_upload,
        android.R.drawable.ic_menu_manage,
        android.R.drawable.ic_menu_gallery
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        dbHelper = DBHelper(this)

        val listView =
            findViewById<ListView>(R.id.listViewProduits)

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

            listView.adapter = ProductAdapter(products)

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
        R.layout.item_product,
        products
    ) {

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view = convertView
                ?: LayoutInflater.from(this@CatalogueActivity)
                    .inflate(
                        R.layout.item_product,
                        parent,
                        false
                    )

            val imageView =
                view.findViewById<ImageView>(R.id.imgProduct)

            val textView =
                view.findViewById<TextView>(R.id.tvProductName)

            val btnAjouter =
                view.findViewById<Button>(R.id.btnAjouterPanier)

            val product = products[position]

            textView.text = "${product.nom} - ${product.prix} DT"

            imageView.setImageResource(
                images[position % images.size]
            )

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
