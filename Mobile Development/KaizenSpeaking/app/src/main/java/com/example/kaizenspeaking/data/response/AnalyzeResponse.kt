package com.example.kaizenspeaking.data.response

data class AnalyzeResponse(
    val score: Score,
    val words: List<String>
)

data class Score(
    val kejelasan: String,
    val diksi: String,
    val kelancaran: String,
    val emosi: String
)
