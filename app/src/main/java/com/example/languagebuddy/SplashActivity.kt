package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash screen that shows the app name/logo for ~1.5 seconds,
 * then moves to the AuthChoiceActivity (login/register).
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, AuthChoiceActivity::class.java))
            finish()
        }, 1500) // 1.5 seconds delay
    }
}
