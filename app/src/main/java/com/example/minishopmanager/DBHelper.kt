
package com.example.minishopmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PanierProduit(
    val id: Int,
    val nom: String,
    val prix: Double,
    val quantite: Int
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "minishop.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE panier (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                prix REAL NOT NULL,
                quantite INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Keep existing cart data when upgrading from version 1.
        if (oldVersion < 2) {
            // No schema changes are needed for this version.
        }
    }

    fun ajouterAuPanier(nom: String, prix: Double): Long {
        val db = writableDatabase

        // If the product already exists, increase its quantity.
        val cursor = db.rawQuery(
            "SELECT id, quantite FROM panier WHERE nom = ?",
            arrayOf(nom)
        )

        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(0)
                val quantite = it.getInt(1) + 1

                val values = ContentValues().apply {
                    put("quantite", quantite)
                }

                db.update(
                    "panier",
                    values,
                    "id = ?",
                    arrayOf(id.toString())
                )

                return id.toLong()
            }
        }

        val values = ContentValues().apply {
            put("nom", nom)
            put("prix", prix)
            put("quantite", 1)
        }

        return db.insert("panier", null, values)
    }

    fun getPanier(): ArrayList<PanierProduit> {
        val produits = ArrayList<PanierProduit>()

        val cursor = readableDatabase.rawQuery(
            "SELECT id, nom, prix, quantite FROM panier",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                produits.add(
                    PanierProduit(
                        id = it.getInt(0),
                        nom = it.getString(1),
                        prix = it.getDouble(2),
                        quantite = it.getInt(3)
                    )
                )
            }
        }

        return produits
    }

    fun modifierQuantite(id: Int, quantite: Int) {
        if (quantite <= 0) {
            supprimerProduit(id)
            return
        }

        val values = ContentValues().apply {
            put("quantite", quantite)
        }

        writableDatabase.update(
            "panier",
            values,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun supprimerProduit(id: Int) {
        writableDatabase.delete(
            "panier",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun viderPanier() {
        writableDatabase.delete("panier", null, null)
    }
}
