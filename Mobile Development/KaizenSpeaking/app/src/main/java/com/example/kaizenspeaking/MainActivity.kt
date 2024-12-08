package com.example.kaizenspeaking

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kaizenspeaking.databinding.ActivityMainBinding
import com.example.kaizenspeaking.ui.auth.SignInActivity
import com.example.kaizenspeaking.utils.UserSession
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var show = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Splash screen setup
        installSplashScreen().setKeepOnScreenCondition { show }
        Handler(Looper.getMainLooper()).postDelayed({
            show = false
        }, 2000)

        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // App bar configuration
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_analyze, R.id.navigation_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Check login status and show dialog if not logged in
//        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        if (!UserSession.isLoggedIn(this)) {
            showLoginDialog()
        }
    }

    private fun showLoginDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_box_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val signInButton: Button = dialog.findViewById(R.id.btnSignIn)
        val buttonClose: ImageView = dialog.findViewById(R.id.btnClose)

        signInButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}