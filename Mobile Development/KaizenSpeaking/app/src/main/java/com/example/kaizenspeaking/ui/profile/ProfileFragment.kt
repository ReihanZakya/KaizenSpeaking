package com.example.kaizenspeaking.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.FragmentProfileBinding
import com.example.kaizenspeaking.ui.auth.utils.APIService
import com.example.kaizenspeaking.utils.UserSession

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private val apiConsumer by lazy { APIService.getService() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
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
            .setIcon(R.drawable.ic_kaizen)
            .setTitle("Tentang Kaizen Speaking")
            .setMessage(Html.fromHtml(getString(R.string.about_kaizen_speaking), Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
        val dialog = builder.create()
        dialog.show()
    }


    private fun displayUserInfo() {
        val userName = UserSession.getUserName(requireContext()) ?: "Unknown Name"
        val userEmail = UserSession.getUserEmail(requireContext()) ?: "unknown@email.com"
        val userId = UserSession.getUserId(requireContext()) ?: "N/A"

        binding.tvName.text = userName
        binding.tvEmail.text = userEmail
        binding.tvUserId.text = "User ID: $userId"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
    }

    private fun performLogout() {
        val builder = AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_kaizen)
            .setTitle("Logout")
            .setMessage("Apakah anda yakin akan keluar dari akun?")
            .setCancelable(true)
            .setPositiveButton("OK") { _, _ ->
                UserSession.logout(requireContext())
                clearEncryptedPreferences()
                clearUserData()
                navigateToLoginScreen()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
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
