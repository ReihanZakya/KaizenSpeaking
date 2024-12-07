package com.example.kaizenspeaking.ui.analyze.Service

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
        private const val ANALYSIS_CHANNEL_ID = "analysis_channel"
        private const val ANALYSIS_CHANNEL_NAME = "Analysis Notifications"
        private const val FAILURE_CHANNEL_ID = "failure_channel"
        private const val FAILURE_CHANNEL_NAME = "Failure Notifications"
        const val EXTRA_TOPIC = "extra_topic"
        const val EXTRA_DEVICE_ID = "extra_device_id"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_FILE_PATH = "extra_file_path"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val topic = intent?.getStringExtra(EXTRA_TOPIC)
        val id = intent?.getStringExtra(EXTRA_USER_ID) ?: intent?.getStringExtra(EXTRA_DEVICE_ID)
        val filePath = intent?.getStringExtra(EXTRA_FILE_PATH)

        if (topic.isNullOrEmpty() || id.isNullOrEmpty() || filePath.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createNotification("Menganalisis rekaman..."))

        serviceScope.launch {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    Log.e("UploadService", "File tidak ditemukan: $filePath")
                    stopSelf()
                    return@launch
                }

                val response = uploadData(topic, id, file)
                if (response != null) {
                    // Kirim broadcast ke fragment
                    val resultIntent = Intent("ANALYZE_RESULT").apply {
                        putExtra("result", response)
                    }
                    LocalBroadcastManager.getInstance(this@UploadForegroundService).sendBroadcast(resultIntent)

                    // Buat notifikasi hasil analisis
                    val navDeepLinkIntent = NavDeepLinkBuilder(this@UploadForegroundService)
                        .setGraph(R.navigation.mobile_navigation)
                        .setDestination(R.id.analyzeResultFragment)
                        .setArguments(Bundle().apply {
                            putParcelable("score", response.first)
                            putString("analyze_message", response.second)
                        })
                        .createPendingIntent()

                    val finishedNotification = createNotification(
                        "Analisis selesai!",
                        navDeepLinkIntent
                    )
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, finishedNotification)
                } else {
                    val failureIntent = Intent("ANALYZE_RESULT_FAILURE")
                    LocalBroadcastManager.getInstance(this@UploadForegroundService).sendBroadcast(failureIntent)

                    showFailureNotification()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showFailureNotification()
            } finally {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun uploadData(topic: String, id: String, file: File): Pair<Score, String>? {
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

    private fun parseAnalyzeResponse(rawResponse: String?): Pair<Score, String>? {
        if (rawResponse.isNullOrEmpty()) return null

        return try {
            val response = Gson().fromJson(rawResponse, AnalyzeResponse::class.java)
            val firstItem = response.data.firstOrNull() ?: return null
            Pair(firstItem.score, firstItem.analize)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createNotification(content: String, pendingIntent: PendingIntent? = null): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rekaman sedang dianalisis")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setAutoCancel(true)

        pendingIntent?.let {
            notificationBuilder.setContentIntent(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        return notificationBuilder.build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val uploadChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel untuk layanan unggahan"
            }
            notificationManager.createNotificationChannel(uploadChannel)

            val analysisChannel = NotificationChannel(
                ANALYSIS_CHANNEL_ID, ANALYSIS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk notifikasi hasil analisis"
            }
            notificationManager.createNotificationChannel(analysisChannel)

            val failureChannel = NotificationChannel(
                FAILURE_CHANNEL_ID, FAILURE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk notifikasi kegagalan"
            }
            notificationManager.createNotificationChannel(failureChannel)
        }
    }

    private fun showFailureNotification() {
        val failureNotification = NotificationCompat.Builder(this, FAILURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Analisis Gagal")
            .setContentText("Terjadi kesalahan saat menganalisis data. Silakan coba lagi.")
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, failureNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
