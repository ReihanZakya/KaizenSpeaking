import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.io.IOException

class AuthToken private constructor(private val context: Context) {
    companion object {
        private const val TOKEN_FILE_NAME = "auth_token.txt"
        private const val TOKEN = "TOKEN"
        private const val TOKEN_VALUE = "TOKEN_VALUE"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AuthToken? = null

        fun getInstance(context: Context): AuthToken {
            return instance ?: synchronized(this) {
                instance ?: AuthToken(context).also {
                    it.initSharedPreferences(context)
                    instance = it
                }
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    fun initSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(TOKEN, Context.MODE_PRIVATE)
    }

    // Shared Preferences token getter and setter
    var token: String?
        set(value) {
            // Save to Shared Preferences
            sharedPreferences.edit().putString(TOKEN_VALUE, value).apply()

            // Save to File
            value?.let { saveTokenToFile(it) }
        }
        get() = sharedPreferences.getString(TOKEN_VALUE, null)

    // Save token to a file in the app's private file directory
    private fun saveTokenToFile(token: String) {
        try {
            val file = File(context.filesDir, TOKEN_FILE_NAME)
            file.writeText(token)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Read token from file
    fun readTokenFromFile(): String? {
        return try {
            val file = File(context.filesDir, TOKEN_FILE_NAME)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Delete token file
    fun deleteTokenFile() {
        try {
            val file = File(context.filesDir, TOKEN_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}