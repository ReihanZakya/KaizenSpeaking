package com.example.kaizenspeaking.ui.auth.data

import android.provider.Settings

data class RegisterBody(
    val full_name : String,
    val email :String,
    val password : String,
    val role : String = "user",
    val device_id : String)

