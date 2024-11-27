package com.example.kaizenspeaking.ui.history.data.remote.response

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("code")
	val code: Int,

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class DataItem(

	@field:SerializedName("transcribe")
	val transcribe: String,

	@field:SerializedName("score")
	val score: Score,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("user_id")
	val userId: String,

	@field:SerializedName("analize")
	val analize: Any,

	@field:SerializedName("topic")
	val topic: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("duration")
	val duration: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("audio_file_url")
	val audioFileUrl: String
)

data class Score(
	@field:SerializedName("kejelasan")
	val kejelasan: String,

	@field:SerializedName("diksi")
	val diksi: String,

	@field:SerializedName("kelancaran")
	val kelancaran: String,

	@field:SerializedName("emosi")
	val emosi: String
)
