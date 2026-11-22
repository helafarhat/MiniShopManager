
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

data class Commande(
    val id: Int,
    val nomClient: String,
    val telephone: String,
    val adresse: String,
    val methodePaiement: String,
    val total: Double,
    val date: Long
)

data class ProduitCommande(
    val nom: String,
    val prix: Double,
    val quantite: Int
)
data class FavoriProduit(
    val nom: String,
    val prix: Double
)
data class Coupon(
    val id: Int,
    val code: String,
    val discountPercent: Int,
    val isUsed: Boolean,
    val createdAt: Long,
    val expiresAt: Long
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "minishop.db", null, 5) {

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

        creerTablesCommandes(db)
        creerTableCoupons(db)
        creerTableFavoris(db)
    }

    private fun creerTablesCommandes(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE commandes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom_client TEXT NOT NULL,
                telephone TEXT NOT NULL,
                adresse TEXT NOT NULL,
                methode_paiement TEXT NOT NULL,
                total REAL NOT NULL,
                date INTEGER NOT NULL,
                coupon_code TEXT,
                discount_amount REAL NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE commande_produits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                commande_id INTEGER NOT NULL,
                nom TEXT NOT NULL,
                prix REAL NOT NULL,
                quantite INTEGER NOT NULL,
                FOREIGN KEY (commande_id)
                    REFERENCES commandes(id)
            )
            """.trimIndent()
        )
    }

    private fun creerTableCoupons(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE coupons (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                discount_percent INTEGER NOT NULL,
                is_used INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        if (oldVersion < 3) {
            creerTablesCommandes(db)
        }

        if (oldVersion < 4) {

            if (oldVersion >= 3) {
                db.execSQL(
                    "ALTER TABLE commandes ADD COLUMN coupon_code TEXT"
                )

                db.execSQL(
                    """
                    ALTER TABLE commandes
                    ADD COLUMN discount_amount REAL NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }

            creerTableCoupons(db)
        }
        if (oldVersion < 5) {
            creerTableFavoris(db)
        }
    }

    // Ajouter un produit au panier
    fun ajouterAuPanier(nom: String, prix: Double): Long {

        val db = writableDatabase

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

    // Récupérer les produits du panier
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

    // Modifier la quantité
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

    // Supprimer un produit
    fun supprimerProduit(id: Int) {

        writableDatabase.delete(
            "panier",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    // Vider le panier
    fun viderPanier() {
        writableDatabase.delete("panier", null, null)
    }

    // Enregistrer une commande et ses produits
    fun enregistrerCommande(
        nom: String,
        telephone: String,
        adresse: String,
        methodePaiement: String,
        produits: List<PanierProduit>,
        total: Double,
        couponCode: String? = null,
        discountAmount: Double = 0.0
    ): Long {

        val db = writableDatabase
        var commandeId = -1L

        db.beginTransaction()

        try {

            val commandeValues = ContentValues().apply {
                put("nom_client", nom)
                put("telephone", telephone)
                put("adresse", adresse)
                put("methode_paiement", methodePaiement)
                put("total", total)
                put("date", System.currentTimeMillis())
                put("coupon_code", couponCode)
                put("discount_amount", discountAmount)
            }

            commandeId = db.insert(
                "commandes",
                null,
                commandeValues
            )

            if (commandeId == -1L) {
                throw Exception("Impossible d'enregistrer la commande")
            }

            for (produit in produits) {

                val produitValues = ContentValues().apply {
                    put("commande_id", commandeId)
                    put("nom", produit.nom)
                    put("prix", produit.prix)
                    put("quantite", produit.quantite)
                }

                val result = db.insert(
                    "commande_produits",
                    null,
                    produitValues
                )

                if (result == -1L) {
                    throw Exception("Impossible d'enregistrer un produit")
                }
            }

            // Vérifier et utiliser le coupon dans la même transaction
            if (!couponCode.isNullOrBlank()) {

                val couponValues = ContentValues().apply {
                    put("is_used", 1)
                }

                val updated = db.update(
                    "coupons",
                    couponValues,
                    """
                    code = ?
                    AND is_used = 0
                    AND expires_at > ?
                    """.trimIndent(),
                    arrayOf(
                        couponCode,
                        System.currentTimeMillis().toString()
                    )
                )

                if (updated == 0) {
                    throw IllegalStateException("Coupon is no longer valid")
                }
            }

            // Vider le panier après l'enregistrement réussi
            db.delete("panier", null, null)

            db.setTransactionSuccessful()

        } finally {
            db.endTransaction()
        }

        return commandeId
    }

    // Récupérer toutes les commandes
    fun getCommandes(): ArrayList<Commande> {

        val commandes = ArrayList<Commande>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, nom_client, telephone, adresse,
                   methode_paiement, total, date
            FROM commandes
            ORDER BY date DESC
            """.trimIndent(),
            null
        )

        cursor.use {

            while (it.moveToNext()) {

                commandes.add(
                    Commande(
                        id = it.getInt(0),
                        nomClient = it.getString(1),
                        telephone = it.getString(2),
                        adresse = it.getString(3),
                        methodePaiement = it.getString(4),
                        total = it.getDouble(5),
                        date = it.getLong(6)
                    )
                )
            }
        }

        return commandes
    }

    // Récupérer les produits d'une commande
    fun getProduitsCommande(
        commandeId: Int
    ): ArrayList<ProduitCommande> {

        val produits = ArrayList<ProduitCommande>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT nom, prix, quantite
            FROM commande_produits
            WHERE commande_id = ?
            """.trimIndent(),
            arrayOf(commandeId.toString())
        )

        cursor.use {

            while (it.moveToNext()) {

                produits.add(
                    ProduitCommande(
                        nom = it.getString(0),
                        prix = it.getDouble(1),
                        quantite = it.getInt(2)
                    )
                )
            }
        }

        return produits
    }

    // Enregistrer un nouveau coupon
    fun enregistrerCoupon(
        code: String,
        discountPercent: Int,
        createdAt: Long,
        expiresAt: Long
    ): Long {

        require(discountPercent in 1..100)

        val values = ContentValues().apply {
            put("code", code)
            put("discount_percent", discountPercent)
            put("is_used", 0)
            put("created_at", createdAt)
            put("expires_at", expiresAt)
        }

        return writableDatabase.insert(
            "coupons",
            null,
            values
        )
    }

    // Récupérer le dernier coupon valide et non utilisé
    fun getCouponActif(): Coupon? {

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, code, discount_percent,
                   is_used, created_at, expires_at
            FROM coupons
            WHERE is_used = 0 AND expires_at > ?
            ORDER BY id DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(System.currentTimeMillis().toString())
        )

        cursor.use {

            if (it.moveToFirst()) {

                return Coupon(
                    id = it.getInt(0),
                    code = it.getString(1),
                    discountPercent = it.getInt(2),
                    isUsed = it.getInt(3) == 1,
                    createdAt = it.getLong(4),
                    expiresAt = it.getLong(5)
                )
            }
        }

        return null
    }

    // Vérifier un coupon par son code
    fun getCouponParCode(code: String): Coupon? {

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, code, discount_percent,
                   is_used, created_at, expires_at
            FROM coupons
            WHERE code = ?
            """.trimIndent(),
            arrayOf(code.trim().uppercase())
        )

        cursor.use {

            if (it.moveToFirst()) {

                return Coupon(
                    id = it.getInt(0),
                    code = it.getString(1),
                    discountPercent = it.getInt(2),
                    isUsed = it.getInt(3) == 1,
                    createdAt = it.getLong(4),
                    expiresAt = it.getLong(5)
                )
            }
        }

        return null
    }
    // ---------------------------------------------------------------------
    // Favoris (produits "cœur") – clé : nom du produit, comme le panier
    // ---------------------------------------------------------------------

    private fun creerTableFavoris(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favoris (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL UNIQUE,
                prix REAL NOT NULL,
                added_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    // Le produit est-il dans les favoris ?
    fun estFavori(nom: String): Boolean {

        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM favoris WHERE nom = ?",
            arrayOf(nom)
        )

        return cursor.use { it.moveToFirst() }
    }

    // Ajoute le produit aux favoris, ou le retire s'il y est déjà.
    // Retourne true si le produit est maintenant un favori.
    fun basculerFavori(nom: String, prix: Double): Boolean {

        val db = writableDatabase

        if (estFavori(nom)) {
            db.delete("favoris", "nom = ?", arrayOf(nom))
            return false
        }

        val values = ContentValues().apply {
            put("nom", nom)
            put("prix", prix)
            put("added_at", System.currentTimeMillis())
        }

        return db.insert("favoris", null, values) != -1L
    }

    // Favoris, du plus récent au plus ancien
    fun getFavoris(): ArrayList<FavoriProduit> {

        val liste = ArrayList<FavoriProduit>()

        readableDatabase.rawQuery(
            "SELECT nom, prix FROM favoris ORDER BY added_at DESC, id DESC",
            null
        ).use {
            while (it.moveToNext()) {
                liste.add(
                    FavoriProduit(
                        nom = it.getString(0),
                        prix = it.getDouble(1)
                    )
                )
            }
        }

        return liste
    }

    // Nombre total d'articles dans le panier (pastille de l'icône panier)
    fun getNombreArticlesPanier(): Int {

        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(quantite), 0) FROM panier",
            null
        )

        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }
}
