package com.example.kaizenspeaking.data.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalyzeResponse(
    val score: Score?,
    val words: List<String>
): Parcelable

@Parcelize
data class Score(
    val kejelasan: String? = "0",
    val diksi: String? = "0",
    val kelancaran: String? = "0",
    val emosi: String? = "0"
): Parcelable
