package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.accountDataStore
import com.example.languagebuddy.data.room.LanguageBuddyDatabase
import com.example.languagebuddy.ui.theme.LanguageBuddyTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onLoginSuccess = {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        },
                        onCreateAccount = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { LanguageBuddyDatabase.getInstance(context) }
    val repo = remember { AccountRepository(database.accountDao(), context.accountDataStore) }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        return when {
            email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                error = "Enter a valid email address"
                false
            }
            password.length < 6 -> {
                error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8EAFE), Color(0xFFF7F7FF))
                )
            )
    ) {
        // Decorative translucent circles
        Box(
            modifier = Modifier
                .padding(start = 40.dp, top = 40.dp)
                .size(220.dp)
                .clip(RoundedCornerShape(110.dp))
                .background(Color.White.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .padding(start = 200.dp, top = 200.dp)
                .size(260.dp)
                .clip(RoundedCornerShape(130.dp))
                .background(Color.White.copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .padding(start = 80.dp, top = 360.dp)
                .size(200.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "LanguageBuddy",
                color = Color(0xFF1A1A1A),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            Text(
                text = "Your personal companion for practicing new languages.",
                color = Color(0xFF555555),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    error = null
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    error = null
                },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (!validate()) return@Button
                    scope.launch {
                        val success = repo.verifyCredentials(email.trim(), password)
                        if (success) {
                            Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            error = "Email or password is incorrect"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(52.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7A77F2),
                    contentColor = Color.White
                ),
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text(text = "Login", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(52.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(28.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(Color(0xFF7A77F2), Color(0xFF7A77F2)))
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7A77F2))
            ) {
                Text(text = "Create Account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
