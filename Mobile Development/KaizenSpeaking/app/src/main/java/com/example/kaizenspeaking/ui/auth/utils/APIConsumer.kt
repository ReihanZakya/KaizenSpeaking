package com.example.kaizenspeaking.ui.auth.utils

import com.example.kaizenspeaking.ui.auth.data.RegisterBody
import com.example.kaizenspeaking.ui.auth.data.AuthResponse
import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.LoginResponse
import com.example.kaizenspeaking.ui.auth.data.UniqueEmailValidationResponse
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.data.ValidateEmailBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIConsumer {
//    @POST("user/validate-unique-email")
//    suspend fun validateEmailAddres(@Body body: ValidateEmailBody) : Response<UniqueEmailValidationResponse>

    @POST("user/register")
    suspend fun registerUser(@Body body: RegisterBody): Response<AuthResponse>

    @POST("user/login")
    suspend fun loginUser(@Body body: LoginBody): Response<AuthResponse>

    @GET("user/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>

    @PUT("user/{userId}")
    suspend fun updateUser(@Path("userId") userId: String, @Body user: User): Response<User>

}