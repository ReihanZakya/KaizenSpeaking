package com.example.kaizenspeaking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.kaizenspeaking.databinding.ActivityMainBinding
import com.example.kaizenspeaking.helper.DeviceIdHelper
import com.example.kaizenspeaking.helper.SharedPreferencesHelper
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.home.HomeFragment
import com.example.kaizenspeaking.ui.home_signed.HomeSignedFragment
import com.example.kaizenspeaking.ui.home_signed.HomeSignedViewModel
import com.example.kaizenspeaking.utils.UserSession

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeSignedViewModel: HomeSignedViewModel

    private var show = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Cek dan buat Device ID
        val deviceIdKey = "device_id"
        var deviceId = SharedPreferencesHelper.getFromSharedPreferences(this, deviceIdKey)
        if (deviceId == null) {
            deviceId = DeviceIdHelper.getUniqueDeviceId(this)
            SharedPreferencesHelper.saveToSharedPreferences(this, deviceIdKey, deviceId)
            Log.d("DeviceID", "Generated new Device ID: $deviceId")
        } else {
            Log.d("DeviceID", "Existing Device ID: $deviceId")
        }

        //Test Change2
        installSplashScreen().setKeepOnScreenCondition{ show }
        Handler(Looper.getMainLooper()).postDelayed({
            show = false
        }, 2000)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_analyze, R.id.navigation_history
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun displayFragment(fragment: Fragment) { supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit() }
}
