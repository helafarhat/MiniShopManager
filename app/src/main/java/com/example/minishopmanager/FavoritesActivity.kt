package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Shows every product the user has hearted. Cards are the same as in the catalogue
 * (item_product_grid), so tapping a card opens the product, the button adds it to the
 * cart and tapping the heart removes it from this list.
 */
class FavoritesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var gridView: GridView
    private lateinit var emptyState: View
    private lateinit var adapter: FavoritesAdapter

    private val favorites = ArrayList<FavoriProduit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_favorites)
        setupScreen()

        dbHelper = DBHelper(this)

        gridView = findViewById(R.id.gridFavorites)
        emptyState = findViewById(R.id.emptyFavorites)

        adapter = FavoritesAdapter()
        gridView.adapter = adapter

        findViewById<Button>(R.id.btnBrowseCollection).setOnClickListener {
            startActivity(Intent(this, CatalogueActivity::class.java))
        }

        refreshFavorites()
    }

    private fun refreshFavorites() {

        favorites.clear()
        favorites.addAll(dbHelper.getFavoris())

        adapter.notifyDataSetChanged()

        val isEmpty = favorites.isEmpty()

        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        gridView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    inner class FavoritesAdapter : BaseAdapter() {

        override fun getCount(): Int = favorites.size

        override fun getItem(position: Int): FavoriProduit = favorites[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view = convertView ?: LayoutInflater
                .from(this@FavoritesActivity)
                .inflate(R.layout.item_product_grid, parent, false)

            val product = favorites[position]
            val image = ProductCatalog.imageFor(this@FavoritesActivity, product.nom)

            view.findViewById<TextView>(R.id.tvProductName).text = product.nom
            view.findViewById<TextView>(R.id.tvProductPrice).text =
                "%.2f DT".format(product.prix)
            view.findViewById<ImageView>(R.id.imgProduct).setImageResource(image)

            // Open the product page
            view.setOnClickListener {
                val intent = Intent(
                    this@FavoritesActivity,
                    ProductDetailsActivity::class.java
                )
                intent.putExtra("product_name", product.nom)
                intent.putExtra("product_price", product.prix)
                intent.putExtra("product_image", image)
                startActivity(intent)
            }

            // Add to the SQLite cart
            view.findViewById<Button>(R.id.btnAjouterPanier).setOnClickListener {

                val result = dbHelper.ajouterAuPanier(product.nom, product.prix)

                if (result != -1L) {
                    Toast.makeText(
                        this@FavoritesActivity,
                        "${product.nom} ajouté au panier !",
                        Toast.LENGTH_SHORT
                    ).show()
                    this@FavoritesActivity.refreshCartBadge()
                } else {
                    Toast.makeText(
                        this@FavoritesActivity,
                        "Erreur lors de l'ajout au panier.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Heart: tapping it here removes the product from the list
            view.findViewById<ImageButton>(R.id.btnFavorite).bindFavorite(
                dbHelper,
                product.nom,
                product.prix
            ) { nowFavorite ->
                if (!nowFavorite) {
                    // Let the heart finish its little animation before the card leaves
                    view.postDelayed({ refreshFavorites() }, 220)
                }
            }

            return view
        }
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized && ::adapter.isInitialized) {
            refreshFavorites()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}