package com.example.minishopmanager

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import android.content.Intent
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Presentation helpers shared by every screen.
 * Nothing in here touches business logic, data or navigation targets.
 */

/**
 * Draws the app edge-to-edge (Android 15+ forces this anyway) while keeping the
 * status-bar and navigation-bar icons dark, because the app is light-only.
 * Call BEFORE super.onCreate().
 */
fun ComponentActivity.enableLightEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(
            Color.TRANSPARENT,
            Color.argb(0x80, 0x1B, 0x1B, 0x1B) // only used on API < 27 where dark icons are unavailable
        )
    )
}

/**
 * Adds the system-bar (and display-cutout) insets to this view's own padding, so content
 * never sits under the status bar, gesture/navigation bar or camera cut-out.
 * The keyboard height is added at the bottom, which replaces windowSoftInputMode=adjustResize
 * on edge-to-edge screens.
 */
fun View.applySystemBarInsets(
    top: Boolean = true,
    bottom: Boolean = true,
    sides: Boolean = true
) {
    val startLeft = paddingLeft
    val startTop = paddingTop
    val startRight = paddingRight
    val startBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        v.setPadding(
            startLeft + if (sides) bars.left else 0,
            startTop + if (top) bars.top else 0,
            startRight + if (sides) bars.right else 0,
            startBottom + if (bottom) maxOf(bars.bottom, ime.bottom) else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/**
 * Standard screen wiring: handles insets on the layout's R.id.screenRoot and makes the
 * top-bar back arrow (R.id.btnTopBack), when the layout has one, go back.
 */
fun ComponentActivity.setupScreen(top: Boolean = true, bottom: Boolean = true) {
    findViewById<View>(R.id.screenRoot)?.applySystemBarInsets(top = top, bottom = bottom)
    findViewById<View>(R.id.btnTopBack)?.setOnClickListener {
        onBackPressedDispatcher.onBackPressed()
    }
    setupTopActions()      // <-- ADD
}
/**
 * Wires the shared top-right actions (favorites + cart with an item-count badge) for any
 * layout that includes @layout/view_top_actions. Layouts without it (e.g. Login) are skipped.
 * The cart shortcut is hidden on the cart screen and the favorites shortcut on the
 * favorites screen, because you are already there.
 */
private fun ComponentActivity.setupTopActions() {
    val favoritesButton = findViewById<View>(R.id.btnTopFavorites) ?: return
    val cartContainer = findViewById<View>(R.id.cartActionContainer) ?: return
    val cartButton = findViewById<View>(R.id.btnTopCart) ?: return

    val onCartScreen = this is CartActivity
    val onFavoritesScreen = this is FavoritesActivity

    cartContainer.visibility = if (onCartScreen) View.GONE else View.VISIBLE
    favoritesButton.visibility = if (onFavoritesScreen) View.GONE else View.VISIBLE

    favoritesButton.setOnClickListener {
        startActivity(Intent(this, FavoritesActivity::class.java))
    }
    cartButton.setOnClickListener {
        startActivity(Intent(this, CartActivity::class.java))
    }

    if (!onCartScreen) {
        // Keep the badge correct when coming back from the cart, a payment, etc.
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                refreshCartBadge()
            }
        })
    }
}

/**
 * Updates the little item-count badge on the cart icon. Call it after adding to the cart.
 * Does nothing on screens that have no cart icon.
 */
fun ComponentActivity.refreshCartBadge() {
    val cartButton = findViewById<View>(R.id.btnTopCart) ?: return
    val badge = findViewById<TextView>(R.id.tvCartBadge) ?: return

    val helper = DBHelper(this)
    val count = try {
        helper.getNombreArticlesPanier()
    } finally {
        helper.close()
    }

    if (count > 0) {
        badge.text = if (count > 99) "99+" else count.toString()
        badge.visibility = View.VISIBLE
        cartButton.contentDescription =
            resources.getQuantityString(R.plurals.cd_cart_items, count, count)
    } else {
        badge.visibility = View.GONE
        cartButton.contentDescription = getString(R.string.cd_cart)
    }
}

/** Shows the heart as filled (favorite) or outlined, and updates its spoken label. */
fun ImageButton.showFavoriteState(isFavorite: Boolean) {
    isSelected = isFavorite
    contentDescription = context.getString(
        if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite
    )
}

/**
 * Makes this heart button toggle [nom] in the favorites table.
 * [onChanged] receives the new state (true = now a favorite).
 */
fun ImageButton.bindFavorite(
    db: DBHelper,
    nom: String,
    prix: Double,
    onChanged: ((Boolean) -> Unit)? = null
) {
    showFavoriteState(db.estFavori(nom))

    setOnClickListener {
        val nowFavorite = db.basculerFavori(nom, prix)
        showFavoriteState(nowFavorite)
        popHeart()
        onChanged?.invoke(nowFavorite)
    }
}

// Small "pop" so the tap feels acknowledged
private fun View.popHeart() {
    animate().cancel()
    scaleX = 0.7f
    scaleY = 0.7f
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(260)
        .setInterpolator(OvershootInterpolator(3f))
        .start()
}
/**
 * Shows [message] under the field (Material outlined input) and clears it as soon as the
 * user edits the text. Falls back to the classic EditText error if the field is not
 * wrapped in a TextInputLayout.
 */
fun EditText.showFieldError(message: String) {
    val layout = findTextInputLayout()
    if (layout == null) {
        error = message
        requestFocus()
        return
    }

    (getTag(R.id.field_error_watcher) as? TextWatcher)?.let { removeTextChangedListener(it) }

    layout.error = message
    requestFocus()

    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            layout.error = null
            removeTextChangedListener(this)
            setTag(R.id.field_error_watcher, null)
        }
    }
    setTag(R.id.field_error_watcher, watcher)
    addTextChangedListener(watcher)
}

private fun EditText.findTextInputLayout(): TextInputLayout? {
    var current = parent
    repeat(3) {
        if (current is TextInputLayout) return current as TextInputLayout
        current = (current as? View)?.parent
    }
    return null
}
