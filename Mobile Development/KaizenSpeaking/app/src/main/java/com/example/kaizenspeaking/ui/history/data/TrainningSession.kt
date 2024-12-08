package com.example.kaizenspeaking.ui.history.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrainingSession(
    val id: String,
    val title: String,
    val date: String,
    val audioUrl: String,
    val duration: String,
    val kejelasan: String?,
    val diksi: String?,
    val kelancaran: String?,
    val emosi: String?,
    val analize: String
) : Parcelable

