package com.example.languagebuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.languagebuddy.data.AccountRepository
import com.example.languagebuddy.data.accountDataStore
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
    val repo = remember { AccountRepository(context.accountDataStore) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Login to continue your language journey.",
            style = MaterialTheme.typography.bodyMedium
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
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        if (error != null) {
            Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
        }

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
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text(text = "Login")
        }

        Button(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Create account")
        }
    }
}
