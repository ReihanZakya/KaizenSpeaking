package com.example.kaizenspeaking.helper

import android.content.Context
import android.provider.Settings
import java.util.UUID

object DeviceIdHelper {
    fun getUniqueDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: UUID.randomUUID().toString()
    }
}