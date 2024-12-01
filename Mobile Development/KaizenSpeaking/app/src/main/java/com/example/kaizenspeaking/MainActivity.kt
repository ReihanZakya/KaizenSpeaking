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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var homeSignedViewModel: HomeSignedViewModel
    private lateinit var navController: NavController

    private var show = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController



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




    }



}
