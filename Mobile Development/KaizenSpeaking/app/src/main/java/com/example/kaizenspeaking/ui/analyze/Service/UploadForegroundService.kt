package com.example.kaizenspeaking.ui.analyze.Service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavDeepLinkBuilder
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.ui.analyze.data.response.AnalyzeResponse
import com.example.kaizenspeaking.ui.analyze.data.response.Score
import com.example.kaizenspeaking.ui.analyze.data.retrofit.ApiConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "upload_channel"
        private const val CHANNEL_NAME = "Upload Service"
        const val EXTRA_TOPIC = "extra_topic"
        const val EXTRA_DEVICE_ID = "extra_device_id"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_FILE_PATH = "extra_file_path"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val topic = intent?.getStringExtra(EXTRA_TOPIC)
        val id = intent?.getStringExtra(EXTRA_USER_ID) ?: intent?.getStringExtra(EXTRA_DEVICE_ID)
        val filePath = intent?.getStringExtra(EXTRA_FILE_PATH)

        if (topic != null && id != null && filePath != null) {
            startForeground(NOTIFICATION_ID, createNotification("Menganalisis rekaman...."))
            serviceScope.launch {
                try {
                    val file = File(filePath)
                    val response = uploadData(topic, id, file)
                    if (response != null) {
                        // Kirim broadcast ke fragment
                        val resultIntent = Intent("ANALYZE_RESULT").apply {
                            putExtra("result", response)
                        }
                        LocalBroadcastManager.getInstance(this@UploadForegroundService).sendBroadcast(resultIntent)

                        // Buat notifikasi
                        val navDeepLinkIntent = NavDeepLinkBuilder(this@UploadForegroundService)
                            .setGraph(R.navigation.mobile_navigation)
                            .setDestination(R.id.analyzeResultFragment)
                            .setArguments(Bundle().apply {
                                putParcelable("result", response)
                            })
                            .createPendingIntent()

                        val finishedNotification = createNotification(
                            "Analisis selesai!",
                            navDeepLinkIntent
                        )
                        val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, finishedNotification)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun uploadData(topic: String, id: String, file: File): AnalyzeResponse? {
        val topicRequestBody = topic.toRequestBody("text/plain".toMediaType())
        val idRequestBody = id.toRequestBody("text/plain".toMediaType())
        val fileRequestBody = file.asRequestBody("audio/m4a".toMediaType())
        val multipartFile = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)

        return try {
            val response = ApiConfig.instance.uploadRecording(topicRequestBody, multipartFile, idRequestBody)
            if (response.isSuccessful) {
                val rawResponse = response.body()?.string()
                parseAnalyzeResponse(rawResponse)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createNotification(content: String, pendingIntent: PendingIntent? = null): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Analisis Rekaman")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun parseAnalyzeResponse(rawResponse: String?): AnalyzeResponse? {
        if (rawResponse.isNullOrEmpty()) return null

        val gson = Gson()
        val lines = rawResponse.split("\n").filter { it.isNotBlank() }

        var score: Score? = null
        val words = mutableListOf<String>()

        for (line in lines) {
            try {
                if (line.contains("score")) {
                    val jsonObject = gson.fromJson(line, JsonObject::class.java)
                    val scoreObject = jsonObject["score"]?.asJsonObject
                    if (scoreObject != null) {
                        score = Score(
                            kejelasan = scoreObject["Kejelasan Berbicara"]?.asString,
                            diksi = scoreObject["Penggunaan Diksi"]?.asString,
                            kelancaran = scoreObject["Kelancaran dan Intonasi"]?.asString,
                            emosi = scoreObject["Emosional dan Keterlibatan Audiens"]?.asString
                        )
                    }
                } else if (line.contains("word")) {
                    val wordObject = gson.fromJson(line, JsonObject::class.java)
                    wordObject["word"]?.let { words.add(it.asString) }
                }
                Log.d("ParseResponse", "Score: $score")
                Log.d("ParseResponse", "Words: $words")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ParseError", "Error parsing response: ${e.message}")
            }
        }

        return if (score != null) {
            AnalyzeResponse(score, words)
        } else {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}