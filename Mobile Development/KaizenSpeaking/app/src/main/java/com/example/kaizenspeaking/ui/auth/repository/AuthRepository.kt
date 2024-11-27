package com.example.kaizenspeaking.ui.auth.repository

import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.RegisterBody
import com.example.kaizenspeaking.ui.auth.data.ValidateEmailBody
import com.example.kaizenspeaking.ui.auth.utils.APIConsumer
import com.example.kaizenspeaking.ui.auth.utils.RequesStatus
import com.example.kaizenspeaking.ui.auth.utils.SimplifiedMessage
import kotlinx.coroutines.flow.flow



class AuthRepository (private val consumer: APIConsumer){
    fun validateEmailAddres(body: ValidateEmailBody) = flow{
        emit(RequesStatus.Waiting)
        val response = consumer.validateEmailAddres(body)
        if (response.isSuccessful){
            emit((RequesStatus.Success(response.body()!!)))
        }else
            emit(RequesStatus.Error(SimplifiedMessage.get(response.errorBody()!!.byteStream().reader().readText())))

    }

    fun registerUser(body: RegisterBody)= flow {
        emit(RequesStatus.Waiting)
        val response = consumer.registerUser(body)
        if (response.isSuccessful) {
            emit(RequesStatus.Success(response.body()!!))
        } else {
            emit(RequesStatus.Error(SimplifiedMessage.get(response.errorBody()!!.byteStream().reader().readText())))
        }
    }

    fun loginUser(body: LoginBody)= flow {
        emit(RequesStatus.Waiting)
        val response = consumer.loginUser(body)
        if (response.isSuccessful) {
            emit(RequesStatus.Success(response.body()!!))
        } else {
            emit(RequesStatus.Error(SimplifiedMessage.get(response.errorBody()!!.byteStream().reader().readText())))
        }
    }
}