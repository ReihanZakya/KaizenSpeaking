package com.example.kaizenspeaking.ui.auth.view_model

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import com.example.kaizenspeaking.ui.auth.utils.RequesStatus
import com.example.kaizenspeaking.utils.UserSession
import kotlinx.coroutines.launch


class SignInActivityViewModel(val authRepository: AuthRepository, val application: Application) :
    ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    private var user: MutableLiveData<User> = MutableLiveData()
    private var loginSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getErrorMessage(): LiveData<HashMap<String, String>> = errorMessage
    fun getUser(): LiveData<User> = user
    fun getLoginSuccess(): LiveData<Boolean> = loginSuccess


    fun loginUser(body: LoginBody, context: Context) {
        viewModelScope.launch {
            authRepository.loginUser(body).collect {
                when (it) {
                    is RequesStatus.Waiting -> {
                        isLoading.value = true

                    }

                    is RequesStatus.Success -> {
                        isLoading.value = false
                        val userData = it.data.data
                        user.value = userData
                        loginSuccess.value = true
                        UserSession.setLoggedIn(context, true)
                        UserSession.setAccessToken(context, userData.access_token)
                        UserSession.setUserId(context, userData.userId)
                        UserSession.setUserName(context, userData.name)

                        // Null-safe username and email setting
                        userData.name?.let { name ->
                            UserSession.setUserName(context, name)
                        }
                        userData.email?.let { email ->
                            UserSession.setUserEmail(context, email)
                        }
                        Log.d(
                            "SignInActivityViewModel",
                            "AccessToken: ${UserSession.getAccessToken(context)}"
                        )
                        Log.d(
                            "SignInActivityViewModel",
                            "UserId: ${UserSession.getUserId(context)}"
                        )
                        Log.d(
                            "SignInActivityViewModel",
                            "Username: ${UserSession.getUserName(context)}"
                        )
                        Log.d(
                            "SignInActivityViewModel",
                            "Email : ${UserSession.getUserEmail(context)}"
                        )
                        showToast(application, "Login Berhasil ")
                    }

                    is RequesStatus.Error -> {
                        isLoading.value = false
                        errorMessage.value = it.message
                        loginSuccess.value = false

                    }
                }

            }

        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}