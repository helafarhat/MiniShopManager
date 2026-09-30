package com.example.minishopmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class CatalogueActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_catalogue)

        val listView = findViewById<ListView>(R.id.listViewProduits)

        val products = arrayOf(
            "Téléphone",
            "Casque Bluetooth",
            "Montre connectée",
            "Chargeur USB",
            "Accessoires",
            "Cosmétiques"
        )

        val images = intArrayOf(
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_btn_speak_now,
            android.R.drawable.ic_menu_recent_history,
            android.R.drawable.ic_menu_upload,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_menu_gallery
        )

        val adapter = ProductAdapter(products, images)

        listView.adapter = adapter

        listView.setOnItemClickListener { parent, _, position, _ ->

            val product = parent
                .getItemAtPosition(position)
                .toString()

            Toast.makeText(
                this,
                "Produit sélectionné : $product",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class ProductAdapter(
        private val products: Array<String>,
        private val images: IntArray
    ) : ArrayAdapter<String>(
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

            textView.text = products[position]

            imageView.setImageResource(images[position])

            return view
        }
    }
}