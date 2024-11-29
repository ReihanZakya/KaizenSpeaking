package com.example.kaizenspeaking.ui.auth.data

import android.devicelock.DeviceId
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName ("_id")
    val id :String,
    val email : String,
    val full_name : String,
    val nick_name : String)

