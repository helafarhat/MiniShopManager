package com.example.minishopmanager

import android.content.Context
import org.json.JSONObject

/**
 * Single source of truth for product photographs.
 * Photos are matched to products by their position in produits.json, exactly as the
 * catalogue has always done, so the Favorites screen shows the same picture.
 */
object ProductCatalog {

    val images = intArrayOf(
        R.drawable.product_phone,
        R.drawable.product_headphones,
        R.drawable.product_watch,
        R.drawable.product_charger,
        R.drawable.product_accessories,
        R.drawable.product_cosmetics
    )

    /** Photo for the product called [nom], or 0 if it is not in produits.json. */
    fun imageFor(context: Context, nom: String): Int {
        return try {
            val jsonText = context.assets.open("produits.json")
                .bufferedReader()
                .use { it.readText() }

            val array = JSONObject(jsonText).getJSONArray("produits")

            for (i in 0 until array.length()) {
                if (array.getJSONObject(i).getString("nom") == nom) {
                    return images[i % images.size]
                }
            }
            0
        } catch (e: Exception) {
            0
        }
    }
}