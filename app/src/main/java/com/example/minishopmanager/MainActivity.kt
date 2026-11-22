package com.example.minishopmanager

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Status-bar inset on the root, navigation-bar inset on the bottom tab bar
        setupScreen(top = true, bottom = false)
        findViewById<View>(R.id.navBar)
            .applySystemBarInsets(top = false, bottom = true, sides = false)

        dbHelper = DBHelper(this)

        // Navigation buttons
        val btnVoirCatalogue =
            findViewById<Button>(R.id.btnVoirCatalogue)

        val navShop = findViewById<TextView>(R.id.navShop)
        val navOrders = findViewById<TextView>(R.id.navOrders)
        val navProfile = findViewById<TextView>(R.id.navProfile)

        val tvExplore =
            findViewById<TextView>(R.id.tvExploreCatalogue)

        val tvSeeAll =
            findViewById<TextView>(R.id.tvSeeAll)

        // Open catalogue
        val openCatalogue = {
            startActivity(
                Intent(this, CatalogueActivity::class.java)
            )
        }

        btnVoirCatalogue.setOnClickListener {
            openCatalogue()
        }

        tvExplore.setOnClickListener {
            openCatalogue()
        }

        tvSeeAll.setOnClickListener {
            openCatalogue()
        }

        navShop.setOnClickListener {
            openCatalogue()
        }

        // Category chips and popular-product cards open the catalogue too
        listOf(
            R.id.btnCategoryAll,
            R.id.btnCategoryTech,
            R.id.btnCategoryAccessories,
            R.id.btnCategoryBeauty,
            R.id.cardPopular1,
            R.id.cardPopular2,
            R.id.cardPopular3
        ).forEach { id ->
            findViewById<View>(id).setOnClickListener { openCatalogue() }
        }

        // Open order history
        navOrders.setOnClickListener {
            startActivity(
                Intent(this, OrderHistoryActivity::class.java)
            )
        }

        // Open profile (guests are sent to the login screen first and
        // land on the Profile screen right after logging in)
        navProfile.setOnClickListener {
            if (Session.isLoggedIn(this)) {
                startActivity(
                    Intent(this, ProfileActivity::class.java)
                )
            } else {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .putExtra(
                            LoginActivity.EXTRA_NEXT,
                            LoginActivity.NEXT_PROFILE
                        )
                )
            }
        }

        // Search field
        val etSearch =
            findViewById<EditText>(R.id.etHomeSearch)

        etSearch.setOnEditorActionListener { _, _, _ ->
            openCatalogue()
            true
        }

        // Home navigation
        findViewById<TextView>(R.id.navHome)
            .setOnClickListener {
                Toast.makeText(
                    this,
                    "You are already on Home",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Show Lucky Spin popup when Home opens
        if (savedInstanceState == null) {
            showLuckySpinPopup()
        }
    }

    override fun onResume() {
        super.onResume()
        updateWelcomeMessage()
    }

    // Personalized welcome for logged-in users, generic one for guests
    private fun updateWelcomeMessage() {
        val studentName = Session.currentUser(this)

        findViewById<TextView>(R.id.tvWelcomeName).text =
            if (studentName.isNullOrBlank()) {
                "Bienvenue dans votre boutique !"
            } else {
                "Bienvenue $studentName dans votre boutique !"
            }
    }

    private fun showLuckySpinPopup() {

        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_lucky_spin)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val closeButton =
            dialog.findViewById<TextView>(R.id.btnCloseSpinPopup)

        val wheel =
            dialog.findViewById<PrizeWheelView>(R.id.popupPrizeWheel)

        val spinButton =
            dialog.findViewById<Button>(R.id.btnPopupSpinNow)

        val resultText =
            dialog.findViewById<TextView>(R.id.tvPopupSpinResult)

        val copyButton =
            dialog.findViewById<Button>(R.id.btnCopyCoupon)

        var generatedCouponCode: String? = null

        // Close popup
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Spin the wheel directly inside the popup
        spinButton.setOnClickListener {

            spinButton.isEnabled = false
            copyButton.visibility = android.view.View.GONE
            resultText.visibility = android.view.View.VISIBLE
            resultText.text = "Good luck! ♡"

            val selectedIndex =
                Random.nextInt(wheel.prizes.size)

            val discount =
                wheel.prizes[selectedIndex]

            val sweep =
                360f / wheel.prizes.size

            val targetRotation =
                (360f - ((selectedIndex + 0.5f) * sweep % 360f)) % 360f

            wheel.animate()
                .rotation(5 * 360f + targetRotation)
                .setDuration(4500)
                .withEndAction {

                    val now = System.currentTimeMillis()

                    val sevenDays =
                        7L * 24 * 60 * 60 * 1000

                    var couponCode: String? = null

                    // Try to save a unique coupon code
                    repeat(5) {

                        if (couponCode == null) {

                            val generatedCode =
                                "MINI${discount}-${Random.nextInt(1000, 10000)}"

                            val saved = dbHelper.enregistrerCoupon(
                                code = generatedCode,
                                discountPercent = discount,
                                createdAt = now,
                                expiresAt = now + sevenDays
                            )

                            if (saved != -1L) {
                                couponCode = generatedCode
                            }
                        }
                    }

                    if (couponCode != null) {

                        generatedCouponCode = couponCode

                        resultText.text =
                            "Congratulations! You won $discount% OFF!\n\n" +
                                    "Your code: $couponCode"

                        copyButton.visibility =
                            android.view.View.VISIBLE

                        Toast.makeText(
                            this,
                            "Your coupon has been saved!",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        resultText.text =
                            "We couldn't save your coupon. Please try again."

                        spinButton.isEnabled = true
                    }
                }
                .start()
        }

        // Copy coupon code
        copyButton.setOnClickListener {

            val code = generatedCouponCode

            if (code != null) {

                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager

                val clip = ClipData.newPlainText(
                    "MiniShopManager Coupon",
                    code
                )

                clipboard.setPrimaryClip(clip)

                Toast.makeText(
                    this,
                    "Coupon copied: $code",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}