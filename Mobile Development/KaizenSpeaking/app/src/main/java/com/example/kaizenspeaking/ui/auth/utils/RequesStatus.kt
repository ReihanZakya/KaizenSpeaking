package com.example.kaizenspeaking.ui.auth.utils

sealed class RequesStatus<out T> {
    object Waiting : RequesStatus<Nothing>()
    data class Success<out T>(val data: T) : RequesStatus<T>()
    data class Error(val message: HashMap<String, String>) : RequesStatus<Nothing>()


}