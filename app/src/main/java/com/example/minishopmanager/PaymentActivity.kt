package com.example.minishopmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    private var subtotal = 0.0
    private var discountAmount = 0.0
    private var appliedCouponCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableLightEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Ordering needs a logged-in user (also covers a restored screen
        // after the app process was killed)
        if (!Session.isLoggedIn(this)) {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra(
                        LoginActivity.EXTRA_NEXT,
                        LoginActivity.NEXT_PAYMENT
                    )
            )
            finish()
            return
        }

        setContentView(R.layout.activity_payment)
        setupScreen()

        dbHelper = DBHelper(this)

        val etName = findViewById<EditText>(R.id.etCustomerName)
        // Pre-fill the delivery name with the logged-in user (still editable)
        etName.setText(Session.currentUser(this))

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)

        val etCoupon = findViewById<EditText>(R.id.etCouponCode)
        val btnApplyCoupon = findViewById<Button>(R.id.btnApplyCoupon)
        val tvCouponMessage = findViewById<TextView>(R.id.tvCouponMessage)

        val rgPayment = findViewById<RadioGroup>(R.id.rgPayment)

        val tvSubtotal = findViewById<TextView>(R.id.tvPaymentSubtotal)
        val tvDiscount = findViewById<TextView>(R.id.tvDiscount)
        val tvTotal = findViewById<TextView>(R.id.tvPaymentTotal)

        val btnConfirm =
            findViewById<Button>(R.id.btnConfirmOrder)

        val products = dbHelper.getPanier()

        subtotal = products.sumOf {
            it.prix * it.quantite
        }

        fun updateTotal() {
            val finalTotal = (subtotal - discountAmount).coerceAtLeast(0.0)

            tvSubtotal.text = "%.2f DT".format(subtotal)
            tvDiscount.text = "-%.2f DT".format(discountAmount)
            tvTotal.text = "%.2f DT".format(finalTotal)
        }

        updateTotal()

        // Reset the applied coupon if the customer edits the code
        etCoupon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                appliedCouponCode = null
                discountAmount = 0.0
                tvCouponMessage.text = ""
                updateTotal()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Apply coupon
        btnApplyCoupon.setOnClickListener {

            val code = etCoupon.text.toString()
                .trim()
                .uppercase()

            if (code.isEmpty()) {
                etCoupon.showFieldError("Enter a coupon code")
                return@setOnClickListener
            }

            val coupon = dbHelper.getCouponParCode(code)

            if (coupon == null) {
                appliedCouponCode = null
                discountAmount = 0.0
                tvCouponMessage.text = "Invalid coupon code."
                updateTotal()
                return@setOnClickListener
            }

            if (coupon.isUsed) {
                appliedCouponCode = null
                discountAmount = 0.0
                tvCouponMessage.text = "This coupon has already been used."
                updateTotal()
                return@setOnClickListener
            }

            if (coupon.expiresAt <= System.currentTimeMillis()) {
                appliedCouponCode = null
                discountAmount = 0.0
                tvCouponMessage.text = "This coupon has expired."
                updateTotal()
                return@setOnClickListener
            }

            if (subtotal <= 0.0) {
                Toast.makeText(
                    this,
                    "Your cart is empty!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Calculate discount
            appliedCouponCode = coupon.code

            discountAmount =
                subtotal * coupon.discountPercent / 100.0

            tvCouponMessage.text =
                "Success! ${coupon.discountPercent}% discount applied ♡"

            updateTotal()

            Toast.makeText(
                this,
                "Coupon applied successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Confirm order
        btnConfirm.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty()) {
                etName.showFieldError("Enter your name")
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                etPhone.showFieldError("Enter your phone number")
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                etAddress.showFieldError("Enter your address")
                return@setOnClickListener
            }

            if (products.isEmpty()) {
                Toast.makeText(
                    this,
                    "Your cart is empty!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val paymentMethod =
                if (rgPayment.checkedRadioButtonId == R.id.rbCard) {
                    "Bank card (demo)"
                } else {
                    "Cash on delivery"
                }

            val finalTotal =
                (subtotal - discountAmount).coerceAtLeast(0.0)

            try {
                val commandeId = dbHelper.enregistrerCommande(
                    nom = name,
                    telephone = phone,
                    adresse = address,
                    methodePaiement = paymentMethod,
                    produits = products,
                    total = finalTotal,
                    couponCode = appliedCouponCode,
                    discountAmount = discountAmount
                )

                if (commandeId != -1L) {

                    val intent = Intent(
                        this,
                        OrderConfirmationActivity::class.java
                    )

                    intent.putExtra("CUSTOMER_NAME", name)
                    intent.putExtra("ORDER_TOTAL", finalTotal)
                    intent.putExtra("PAYMENT_METHOD", paymentMethod)
                    intent.putExtra("ORDER_ID", commandeId.toInt())

                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "Unable to save your order.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
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