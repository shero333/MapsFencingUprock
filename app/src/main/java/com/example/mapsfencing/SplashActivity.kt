package com.example.mapsfencing

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mapsfencing.admin.AdminMapsActivity
import com.example.mapsfencing.authentication.LoginActivity
import com.example.mapsfencing.preferences.PreferenceManager
import com.example.mapsfencing.user.UserMapsActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen: SplashScreen = installSplashScreen()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_splash)

        splashScreen.setKeepOnScreenCondition { true }

        preferenceManager = PreferenceManager(this@SplashActivity)

        if (preferenceManager.isLoggedIn()){

            Log.d("usernameSplash", "username: "+preferenceManager.getuser().username)

            if (preferenceManager.getuser().username!!.contains("admin",true)){
                startActivity(Intent(this@SplashActivity, AdminMapsActivity::class.java))
                finish()
            }else{

                startActivity(Intent(this@SplashActivity, UserMapsActivity::class.java))
                finish()
            }

        }else{
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}