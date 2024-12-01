package com.example.kaizenspeaking.utils

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREFERENCE_NAME = "user_session"
    private const val IS_LOGGED_IN = "is_logged_in"
    private val loginStateListeners = mutableListOf<LoginStateListener>()

    interface LoginStateListener {
        fun onLoginStateChanged(isLoggedIn: Boolean)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn)
        editor.apply()

        // Notify all listeners about login state change
        loginStateListeners.forEach { it.onLoginStateChanged(isLoggedIn) }
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun addLoginStateListener(listener: LoginStateListener) {
        loginStateListeners.add(listener)
    }

    fun removeLoginStateListener(listener: LoginStateListener) {
        loginStateListeners.remove(listener)
    }
}