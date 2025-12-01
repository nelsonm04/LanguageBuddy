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

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageBuddyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        },
                        onHaveAccount = {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onHaveAccount: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AccountRepository(context.accountDataStore) }
    val scope = rememberCoroutineScope()

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        return when {
            firstName.isBlank() -> {
                error = "First name is required"
                false
            }
            lastName.isBlank() -> {
                error = "Last name is required"
                false
            }
            email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                error = "Enter a valid email address"
                false
            }
            password.length < 6 -> {
                error = "Password must be at least 6 characters"
                false
            }
            confirmPassword != password -> {
                error = "Passwords must match"
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
            text = "Create account",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Join LanguageBuddy and track your practice progress.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                error = null
            },
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                error = null
            },
            label = { Text("Last name") },
            modifier = Modifier.fillMaxWidth()
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
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                error = null
            },
            label = { Text("Confirm password") },
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
                    val displayName = listOf(firstName.trim(), lastName.trim()).filter { it.isNotEmpty() }.joinToString(" ")
                    val created = repo.registerAccount(displayName, email.trim(), password)
                    if (!created) {
                        error = "An account with this email already exists"
                    } else {
                        Toast.makeText(context, "Account created", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            Text(text = "Register")
        }

        Button(
            onClick = onHaveAccount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "I already have an account")
        }
    }
}
