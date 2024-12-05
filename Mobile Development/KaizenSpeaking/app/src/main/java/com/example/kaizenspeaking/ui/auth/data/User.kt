package com.example.kaizenspeaking.ui.auth.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id")
    val id: String,
    val email: String,
    val full_name: String,
    val name: String,
    val access_token: String,
    val userId: String
)

