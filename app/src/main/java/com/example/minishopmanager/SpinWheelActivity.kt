
package com.example.minishopmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SpinWheelActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var generatedCouponCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spin_wheel)
        setupScreen()

        dbHelper = DBHelper(this)

        val wheel = findViewById<PrizeWheelView>(R.id.prizeWheel)
        val btnSpin = findViewById<Button>(R.id.btnSpin)
        val tvResult = findViewById<TextView>(R.id.tvSpinResult)
        val btnCopy = findViewById<Button>(R.id.btnCopyCoupon)

        btnCopy.visibility = View.GONE

        // Spin the wheel
        btnSpin.setOnClickListener {

            btnSpin.isEnabled = false
            btnCopy.visibility = View.GONE
            generatedCouponCode = null

            tvResult.text = "Good luck! ♡"

            val selectedIndex =
                Random.nextInt(wheel.prizes.size)

            val discount = wheel.prizes[selectedIndex]

            val sweep = 360f / wheel.prizes.size

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

                        tvResult.text =
                            "Congratulations! You won $discount% OFF!\n\n" +
                                    "Your code: $couponCode\n\n" +
                                    "Valid for 7 days ♡"

                        btnCopy.visibility = View.VISIBLE

                        Toast.makeText(
                            this,
                            "Your coupon has been saved!",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        tvResult.text =
                            "We couldn't save your coupon. Please try again."

                        btnSpin.isEnabled = true
                    }
                }
                .start()
        }

        // Copy coupon code
        btnCopy.setOnClickListener {

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
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
