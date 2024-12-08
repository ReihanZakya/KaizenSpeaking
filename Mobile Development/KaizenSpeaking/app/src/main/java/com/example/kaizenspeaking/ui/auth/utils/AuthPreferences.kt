package com.example.kaizenspeaking.ui.auth.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object AuthPreferences {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"

    fun saveAuthData(context: Context, accessToken: String, userId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun logAuthData(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken: String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val userId: String? = sharedPreferences.getString(KEY_USER_ID, null)
        Log.d("AuthPreferences", "Access Token: $accessToken")
        Log.d("AuthPreferences", "User ID: $userId")
    }
}
