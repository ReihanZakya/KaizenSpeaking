package com.example.kaizenspeaking.ui.auth.utils

import org.json.JSONException
import org.json.JSONObject

object SimplifiedMessage {
    fun get(stringMessage: String): HashMap<String, String> {
        val messages = HashMap<String, String>()
        val jsonObject = JSONObject(stringMessage)

        try {
            val jsonMessages = jsonObject.getJSONObject("detail")
            if (jsonMessages.has("message")) {
                messages["message"] = jsonMessages.getString("message")
            } else {
                messages["message"] = "Unexpected error occurred. Please try again."
            }
        } catch (e: JSONException) {
            messages["message"] = "Unexpected error occurred. Please try again."
        }

        return messages
    }
}
