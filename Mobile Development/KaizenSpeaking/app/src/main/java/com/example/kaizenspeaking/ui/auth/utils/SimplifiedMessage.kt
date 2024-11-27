package com.example.kaizenspeaking.ui.auth.utils

import org.json.JSONObject
import org.json.JSONException

object SimplifiedMessage {
    fun get(stringMessage: String): HashMap<String, String> {
        val messages = HashMap<String, String>()
        val jsonObject = JSONObject(stringMessage)

        try {
            val jsonMessages = JSONObject("detail")
            jsonMessages.keys().forEachRemaining { messages[it] = jsonMessages.getString(it) }

            // Try multiple possible error message keys

        } catch (e: JSONException) {
            // If parsing fails, use the entire error body
            messages["detail"] = jsonObject.getString("detail")
        }

        return messages
    }
}