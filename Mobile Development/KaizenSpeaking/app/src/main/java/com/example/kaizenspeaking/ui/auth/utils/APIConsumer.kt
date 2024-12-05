package com.example.kaizenspeaking.ui.auth.utils

import com.example.kaizenspeaking.ui.auth.data.AuthResponse
import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.RegisterBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface APIConsumer {
//    @POST("user/validate-unique-email")
//    suspend fun validateEmailAddres(@Body body: ValidateEmailBody) : Response<UniqueEmailValidationResponse>

    @POST("user/register")
    suspend fun registerUser(@Body body: RegisterBody): Response<AuthResponse>

    @POST("user/login")
    suspend fun loginUser(@Body body: LoginBody): Response<AuthResponse>


}