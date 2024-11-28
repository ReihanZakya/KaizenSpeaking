package com.example.kaizenspeaking.ui.history.data.remote.retrofit

import com.example.kaizenspeaking.ui.history.data.remote.response.HistoryResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiService {
    @GET("speech/speech-history/{user_id}")
    suspend fun getHistory(
        @Header("Authorization") token: String,
        @Path("user_id") user_id: String
    ): HistoryResponse
}