package com.example.kaizenspeaking.ui.auth.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName ("_id")
    val id :String,
    val name : String,
    val email : String)
