package com.example.kaizenspeaking.ui.history.data.remote

import com.example.kaizenspeaking.ui.history.data.Result
import com.example.kaizenspeaking.ui.history.data.remote.response.DataItem
import com.example.kaizenspeaking.ui.history.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class Repository private constructor(
    private val apiService: ApiService
) {
    suspend fun getAllHistory(token: String, userId: String): Result<List<DataItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = "Bearer $token"
                val response = apiService.getHistory(token, userId)
                if (response.status == "success") {
                    Result.Success(response.data)
                } else {
                    Result.Error(response.message)
                }
            } catch (e: HttpException) {
                Result.Error("Http Exception: ${e.message}")
            } catch (e: Exception) {
                Result.Error("An error occured: ${e.message}")
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: Repository? = null
        fun getInstance(
            apiService: ApiService
        ): Repository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Repository(apiService)
        }.also { INSTANCE = it }
    }
}