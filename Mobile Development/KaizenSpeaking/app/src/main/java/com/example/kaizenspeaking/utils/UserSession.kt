package com.example.kaizenspeaking.utils

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREFERENCE_NAME = "user_session"
    private const val IS_LOGGED_IN = "is_logged_in"
    private const val ACCESS_TOKEN = "access_token"
    private const val USER_ID = "user_id"
    private const val USER_EMAIL = "user_email"
    private const val USER_NAME = "user_name"
    private const val IS_FIRST_TIME_USER = "is_first_time_user"
    private const val ONBOARDING_SHOWN = "onboarding_shown"

    private val loginStateListeners = mutableListOf<LoginStateListener>()

    interface LoginStateListener {
        fun onLoginStateChanged(isLoggedIn: Boolean)
    }

    /**
     * Performs a comprehensive logout by clearing all user-related data
     * @param context Application context
     */
    fun logout(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Clear all user-related data
        editor.remove(IS_LOGGED_IN)
        editor.remove(ACCESS_TOKEN)
        editor.remove(USER_ID)
        editor.remove(USER_EMAIL)
        editor.remove(USER_NAME)

        setFirstTimeUser(context, true)
        setOnboardingShown(context, false)

        // Commit the changes
        editor.apply()

        // Notify login state listeners
        loginStateListeners.forEach { it.onLoginStateChanged(false) }
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn)
        editor.apply()

        // Notify all listeners about login state change
        loginStateListeners.forEach { it.onLoginStateChanged(isLoggedIn) }
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun setAccessToken(context: Context, token: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(ACCESS_TOKEN, token)
        editor.apply()
    }

    fun getAccessToken(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(ACCESS_TOKEN, null)
    }

    fun setUserId(context: Context, userId: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(USER_ID, userId)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_ID, null)
    }

    // New methods for storing additional user information
    fun setUserEmail(context: Context, email: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun getUserEmail(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_EMAIL, null)
    }

    fun setUserName(context: Context, name: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(USER_NAME, name)
        editor.apply()
    }

    fun getUserName(context: Context): String? {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_NAME, null)
    }

    fun isFirstTimeUser(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_FIRST_TIME_USER, true)
    }


    fun setFirstTimeUser(context: Context, isFirstTime: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(IS_FIRST_TIME_USER, isFirstTime)
        editor.apply()
    }

    fun setOnboardingShown(context: Context, shown: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(ONBOARDING_SHOWN, shown)
        editor.apply()
    }


}