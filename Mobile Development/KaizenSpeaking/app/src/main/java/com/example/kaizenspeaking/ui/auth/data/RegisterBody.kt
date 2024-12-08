package com.example.kaizenspeaking.ui.auth.data

data class RegisterBody(
    val full_name: String,
    val nickname: String,
    val email: String,
    val password: String,
    val role: String = "user"
)

