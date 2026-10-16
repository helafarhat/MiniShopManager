
package com.example.minishopmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "minishop.db", null, 1) {

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
        db.execSQL("DROP TABLE IF EXISTS panier")
        onCreate(db)
    }

    fun ajouterAuPanier(nom: String, prix: Double): Long {
        val values = ContentValues().apply {
            put("nom", nom)
            put("prix", prix)
            put("quantite", 1)
        }

        return writableDatabase.insert("panier", null, values)
    }
}
