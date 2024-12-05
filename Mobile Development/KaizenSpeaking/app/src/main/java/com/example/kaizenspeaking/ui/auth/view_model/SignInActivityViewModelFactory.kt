package com.example.kaizenspeaking.ui.auth.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import java.security.InvalidParameterException

class SignInActivityViewModelFactory(
    private val authRepository: AuthRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInActivityViewModel::class.java)) {
            return SignInActivityViewModel(authRepository, application) as T
        }

        throw InvalidParameterException("Unable to Construch SignInFragmentViewModel")
    }
}