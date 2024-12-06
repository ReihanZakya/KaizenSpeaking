package com.example.kaizenspeaking.ui.auth.data

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("access token")
    val accessToken: String,
    @SerializedName("token type")
    val tokenType: String,
    @SerializedName("userId")
    val userId: String
)
