package com.example.minishopmanager

import android.content.Context

/**
 * Single source of truth for "is someone logged in?".
 *
 * It reuses the SharedPreferences file and keys LoginActivity already writes
 * ("MiniShopPrefs" / "STUDENT" / "REMEMBER"), so no data migration is needed.
 *
 * - After a login the name is held in memory for the life of the app process.
 * - If the user ticked "Se souvenir de moi", the saved name also restores the
 *   session the next time the app is opened.
 */
object Session {

    private const val PREFS_NAME = "MiniShopPrefs"
    private const val KEY_STUDENT = "STUDENT"
    private const val KEY_REMEMBER = "REMEMBER"

    private var currentName: String? = null

    /** Name of the logged-in user, or null for a guest. */
    fun currentUser(context: Context): String? {
        currentName?.let { return it }

        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_REMEMBER, false)) {
            val saved = prefs.getString(KEY_STUDENT, null)
            if (!saved.isNullOrBlank()) {
                currentName = saved
                return saved
            }
        }

        return null
    }

    fun isLoggedIn(context: Context): Boolean = currentUser(context) != null

    /** Called by LoginActivity after a successful login. */
    fun logIn(name: String) {
        currentName = name
    }

    /** Back to guest mode; also forgets the remembered name. */
    fun logOut(context: Context) {
        currentName = null

        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_STUDENT)
            .putBoolean(KEY_REMEMBER, false)
            .apply()
    }
}