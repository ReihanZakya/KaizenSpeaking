package com.example.kaizenspeaking.data.retrofit

import com.example.kaizenspeaking.data.response.AnalyzeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("speech/upload-speech")
    @Multipart
    suspend fun uploadRecording(
        @Part("topic") topic: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): Response<ResponseBody>
}