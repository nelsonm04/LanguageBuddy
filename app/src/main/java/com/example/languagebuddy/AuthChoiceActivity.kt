package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme

class AuthChoiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthChoiceScreen(
                        onLogin = { startActivity(Intent(this, LoginActivity::class.java)) },
                        onRegister = { startActivity(Intent(this, RegisterActivity::class.java)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthChoiceScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            Color.White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        DecorativeBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LB",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "LanguageBuddy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your personal companion for practicing new languages.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLogin,
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp)
                ) {
                    Text(text = "Login", fontSize = 16.sp)
                }
                OutlinedButton(
                    onClick = onRegister,
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp)
                ) {
                    Text(text = "Create account", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun DecorativeBackground(modifier: Modifier = Modifier) {
    // Fix: Access theme colors in the Composable scope, not the DrawScope
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val maxDim = size.maxDimension

        drawCircle(
            color = primary.copy(alpha = 0.08f),
            radius = maxDim * 0.45f,
            center = androidx.compose.ui.geometry.Offset(x = size.width * 0.2f, y = size.height * 0.1f)
        )
        drawCircle(
            color = secondary.copy(alpha = 0.06f),
            radius = maxDim * 0.35f,
            center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.25f)
        )
        drawCircle(
            color = primary.copy(alpha = 0.05f),
            radius = maxDim * 0.5f,
            center = androidx.compose.ui.geometry.Offset(x = size.width * 0.7f, y = size.height * 0.95f)
        )
    }

}
