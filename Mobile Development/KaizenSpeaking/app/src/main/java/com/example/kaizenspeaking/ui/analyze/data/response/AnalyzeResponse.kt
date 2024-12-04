package com.example.kaizenspeaking.ui.analyze.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalyzeResponse(
    @SerializedName("data")
    val data: List<DataItem>
) : Parcelable


@Parcelize
data class DataItem(
    @field:SerializedName("score")
    val score: Score,

    @field:SerializedName("analysis_message")
    val analize: String
) : Parcelable

@Parcelize
data class Score(
    @SerializedName("Kejelasan Berbicara")
    val kejelasan: String? = "0",

    @SerializedName("Penggunaan Diksi")
    val diksi: String? = "0",

    @SerializedName("Kelancaran dan Intonasi")
    val kelancaran: String? = "0",

    @SerializedName("Emosional dan Keterlibatan Audiens")
    val emosi: String? = "0",
) : Parcelable



