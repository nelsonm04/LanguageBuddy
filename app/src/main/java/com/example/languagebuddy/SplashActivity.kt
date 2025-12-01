package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                LaunchedEffect(Unit) {
                    startActivity(Intent(this@SplashActivity, AuthChoiceActivity::class.java))
                    finish()
                }
            }
        }
    }
}
