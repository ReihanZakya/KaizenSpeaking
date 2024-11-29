package com.example.kaizenspeaking.ui.auth.data


data class LoginResponse(
        val token: String, // Assuming you have a token in the response
        val fullName: String, // Add full name to the response
        val email: String,
        // Add other relevant fields as needed
    )

