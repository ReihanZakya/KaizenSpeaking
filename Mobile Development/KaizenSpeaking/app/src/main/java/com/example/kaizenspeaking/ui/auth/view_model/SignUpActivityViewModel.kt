package com.example.kaizenspeaking.ui.auth.view_model

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenspeaking.ui.auth.data.RegisterBody
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import com.example.kaizenspeaking.ui.auth.utils.RequesStatus
import kotlinx.coroutines.launch

class SignUpActivityViewModel(
    val authRepository: AuthRepository,
    val application: Application
) : ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    private var isUniqueEmail: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var user: MutableLiveData<User> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getErrorMessage(): LiveData<HashMap<String, String>> = errorMessage
    fun getIsUniqueEmail(): LiveData<Boolean> = isUniqueEmail
    fun getUser(): LiveData<User> = user

//        fun validateEmailAddress(body: ValidateEmailBody) {
//            viewModelScope.launch {
//                authRepository.validateEmailAddres(body).collect { response ->
//                    when (response) {
//                        is RequesStatus.Waiting -> {
//                            isLoading.value = true
//                        }
//                        is RequesStatus.Success -> {
//                            isLoading.value = false
//                            isUniqueEmail.value = response.data.isUnique
//                        }
//                        is RequesStatus.Error -> {
//                            isLoading.value = false
//                            errorMessage.value = response.message
//                        }
//                    }
//                }
//            }
//        }

    fun registerUser(body: RegisterBody) {
        viewModelScope.launch {

            authRepository.registerUser(body).collect {
                when (it) {
                    is RequesStatus.Waiting -> {
                        isLoading.value = true
                    }

                    is RequesStatus.Success -> {
                        isLoading.value = false
                        user.value = it.data.data
                        showToast(application, "Daftar berhasil")
                    }

                    is RequesStatus.Error -> {
                        isLoading.value = false
                        errorMessage.value = it.message
                    }
                }
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}