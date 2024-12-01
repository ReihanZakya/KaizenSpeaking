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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

        fetchAndDisplayUserInfo()

        return binding.root
    }

    private fun fetchAndDisplayUserInfo() {
        val userId = UserSession.getUserId(requireContext())
        val token = UserSession.getAccessToken(requireContext())

        if (userId != null && token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = apiConsumer.getUser(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()
                        displayUserInfo(user)
                    } else {
                        // Handle error
                    }
                }
            }
        }
    }

    private fun displayUserInfo(user: User?) {
        // Set TextViews with user information
        binding.tvName.text = user?.full_name ?: "Unknown Name"
        binding.tvEmail.text = user?.email ?: "unknown@email.com"
        binding.tvUserId.text = "User ID: ${user?.id ?: "N/A"}"
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
        
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sembunyikan BottomNavigationView
        hideBottomNavigation()

        // Tambahkan fungsionalitas tombol kembali
        val backButton = view.findViewById<ImageView>(R.id.back_btn)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Tambahkan klik listener untuk tvAbout
        val tvAbout = view.findViewById<TextView>(R.id.tvAbout)
        tvAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val description = getString(R.string.about_kaizen_speaking) // Mengambil teks dari resource string
        builder.setMessage(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() } // Tombol OK
            .setCancelable(true) // Membuat dialog dapat ditutup dengan klik di luar
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tampilkan kembali BottomNavigationView saat fragment ini dihancurkan
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        // Ambil referensi BottomNavigationView dari Activity utama
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        // Tampilkan kembali BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
    }
}
