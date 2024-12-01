package com.example.kaizenspeaking.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.utils.APIConsumer
import com.example.kaizenspeaking.ui.auth.utils.APIService
import com.example.kaizenspeaking.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class KaizenUserDataService(private val apiConsumer: APIConsumer, private val context: Context) {

    /**
     * Fetches the user data from the Kaizen API using the stored user ID.
     *
     * @return Result containing the user data or an error
     */
    suspend fun getUserData(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val userId = UserSession.getUserId(context) ?: return@withContext Result.failure(Exception("User ID not found in SharedPreferences"))

            val response: Response<User> = apiConsumer.getUser(userId)

            if (response.isSuccessful) {
                response.body()?.let { user ->
                    return@withContext Result.success(user)
                } ?: return@withContext Result.failure(Exception("Empty response body"))
            } else {
                return@withContext Result.failure(Exception("Failed to fetch user data: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    /**
     * Updates the user data in the Kaizen API and the local SharedPreferences.
     *
     * @param user The updated user data
     * @return Result indicating whether the update was successful or not
     */
    suspend fun updateUserData(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response: Response<User> = apiConsumer.updateUser(user.id, user)

            if (response.isSuccessful) {
                response.body()?.let { updatedUser ->
                    updateLocalUserData(updatedUser)
                    return@withContext Result.success(Unit)
                } ?: return@withContext Result.failure(Exception("Empty response body"))
            } else {
                return@withContext Result.failure(Exception("Failed to update user data: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun updateLocalUserData(user: User) {
        UserSession.setUserId(context, user.id)
        UserSession.setUserEmail(context, user.email)
        UserSession.setUserName(context, user.full_name)
    }
}

// Example usage
suspend fun main() {
    val apiService = APIService.getService()
    val userDataService = KaizenUserDataService(apiService, MyApplication.context)

    // Fetch user data
    val userDataResult = userDataService.getUserData()
    userDataResult.onSuccess { user ->
        println("User data fetched: $user")

        // Update user data
        val updatedUser = user.copy(full_name = "John Doe Updated")
        val updateResult = userDataService.updateUserData(updatedUser)
        updateResult.onSuccess {
            println("User data updated successfully")
        }.onFailure { error ->
            println("Failed to update user data: ${error.message}")
        }
    }.onFailure { error ->
        println("Failed to fetch user data: ${error.message}")
    }
}

@SuppressLint("StaticFieldLeak")
object MyApplication {
    lateinit var context: Context
}