package com.example.kaizenspeaking.ui.auth.data

data class LoginResponse(
    val accessToken: String,
    val userId: String,
    val data: User
)

