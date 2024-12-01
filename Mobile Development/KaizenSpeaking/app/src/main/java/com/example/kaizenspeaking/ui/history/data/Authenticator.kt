package com.example.kaizenspeaking.ui.history.data

import android.content.Context
import com.example.kaizenspeaking.utils.UserSession

object Authenticator {

    fun getToken(context: Context): String? {
        return UserSession.getAccessToken(context)
    }

    fun getUserId(context: Context): String? {
        return UserSession.getUserId(context)
    }

    fun getEmail(context: Context): String? {
        return UserSession.getUserEmail(context)
    }

    fun getPassword(context: Context): String {
        return "anam1234"  // Example, do not hardcode password in production apps.
    }

    fun getDeviceId(context: Context): String {
        return "5327b5c7f7ac05d8"  // Example, should get real device ID in production apps.
    }
}
