package com.example.adkpractical.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.adkpractical.models.ScanHistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesManager {
    private const val PREF_NAME = "adk_app_prefs"
    private const val KEY_HISTORY_PREFIX = "history_"
    private const val KEY_WISHLIST_PREFIX = "wishlist_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
    }

    // --- Scan History ---
    fun getScanHistory(context: Context): List<ScanHistoryItem> {
        val uid = getCurrentUserId()
        val json = getPrefs(context).getString("$KEY_HISTORY_PREFIX$uid", null) ?: return emptyList()
        val type = object : TypeToken<List<ScanHistoryItem>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun saveScanHistoryItem(context: Context, item: ScanHistoryItem) {
        val uid = getCurrentUserId()
        val currentHistory = getScanHistory(context).toMutableList()
        currentHistory.removeAll { it.barcode == item.barcode }
        currentHistory.add(0, item)
        val json = Gson().toJson(currentHistory)
        getPrefs(context).edit().putString("$KEY_HISTORY_PREFIX$uid", json).apply()
    }

    // --- Wish-to-Visit ---
    fun getWishlist(context: Context): Set<String> {
        val uid = getCurrentUserId()
        return getPrefs(context).getStringSet("$KEY_WISHLIST_PREFIX$uid", emptySet()) ?: emptySet()
    }

    fun toggleWishlist(context: Context, cca2: String): Boolean {
        val uid = getCurrentUserId()
        val current = getWishlist(context).toMutableSet()
        val isAdded: Boolean
        if (current.contains(cca2)) {
            current.remove(cca2)
            isAdded = false
        } else {
            current.add(cca2)
            isAdded = true
        }
        getPrefs(context).edit().putStringSet("$KEY_WISHLIST_PREFIX$uid", current).apply()
        return isAdded
    }

    fun isWishlisted(context: Context, cca2: String): Boolean {
        return getWishlist(context).contains(cca2)
    }
}