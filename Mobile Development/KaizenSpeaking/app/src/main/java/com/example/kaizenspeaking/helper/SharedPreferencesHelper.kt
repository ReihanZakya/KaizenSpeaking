package com.example.kaizenspeaking.helper

import android.content.Context

object SharedPreferencesHelper {
    private const val PREF_NAME = "app_prefs"

    fun saveToSharedPreferences(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getFromSharedPreferences(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }
}
