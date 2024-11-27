package com.example.kaizenspeaking.ui.auth.view_model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import com.example.kaizenspeaking.ui.auth.utils.RequesStatus
import kotlinx.coroutines.launch

class SignInActivityViewModel(val authRepository : AuthRepository, val application: Application) : ViewModel() {
    private var isLoading : MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage : MutableLiveData<HashMap<String,String>> = MutableLiveData()
    private var user : MutableLiveData<User> = MutableLiveData()
    private var loginSuccess: MutableLiveData<Boolean> = MutableLiveData()

    fun getIsLoading():LiveData<Boolean>    = isLoading
    fun getErrorMessage():LiveData<HashMap<String,String>> = errorMessage
    fun getUser() : LiveData<User> = user
    fun getLoginSuccess(): LiveData<Boolean> = loginSuccess


    fun loginUser(body: LoginBody) {
        viewModelScope.launch {
            authRepository.loginUser(body).collect {
                when (it) {
                    is RequesStatus.Waiting -> {
                        isLoading.value = true

                    }
                    is RequesStatus.Success -> {
                        isLoading.value = false
                        user.value = it.data.user

                        val authToken = AuthToken.getInstance(application.baseContext)
                        authToken.token  = it.data.token

                        loginSuccess.value = true
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

}