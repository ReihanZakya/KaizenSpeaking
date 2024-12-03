package com.example.kaizenspeaking.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kaizenspeaking.databinding.FragmentProfileBinding
import com.example.kaizenspeaking.ui.auth.SignInActivity
import com.example.kaizenspeaking.ui.auth.utils.APIService
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.utils.UserSession
import com.example.kaizenspeaking.MainActivity
import android.content.SharedPreferences
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private val apiConsumer by lazy { APIService.getService() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Encrypted SharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Add logout functionality
        binding.logoutButton.setOnClickListener {
            performLogout()
        }

        displayUserInfo()

        return binding.root
    }



    private fun displayUserInfo() {
        val userName = UserSession.getUserName(requireContext()) ?: "Unknown Name"
        val userEmail = UserSession.getUserEmail(requireContext()) ?: "unknown@email.com"
        val userId = UserSession.getUserId(requireContext()) ?: "N/A"

        binding.tvName.text = userName
        binding.tvEmail.text = userEmail
        binding.tvUserId.text = "User ID: $userId"
    }


    private fun performLogout() {
        // 1. Clear User Session completely
        UserSession.logout(requireContext())

        // 2. Clear Authentication Tokens

        // 3. Clear Encrypted Preferences
        clearEncryptedPreferences()

        // 4. Clear User-related Data
        clearUserData()

        // 5. Navigate back to login/home screen
        navigateToLoginScreen()
    }

    private fun clearEncryptedPreferences() {
        // Clear all encrypted shared preferences
        encryptedSharedPreferences.edit().clear().apply()
    }

    private fun clearUserData() {
        // Clear any local database or cached user data
        // Example: If you're using Room database
        // userDao.deleteAllUserData()

        // Clear any cached files or temporary user-related data
        // context?.cacheDir?.deleteRecursively()
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
