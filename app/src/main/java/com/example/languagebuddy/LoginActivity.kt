package com.example.languagebuddy

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val pass = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnLogin)

        btn.setOnClickListener {
            val e = email.text.toString().trim()
            val p = pass.text.toString()
            if (e.isEmpty() || p.isEmpty()) {
                toast("Email and password are required"); return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                toast("Invalid email"); return@setOnClickListener
            }
            if (p.length < 6) {
                toast("Password must be at least 6 chars"); return@setOnClickListener
            }
            toast("Login success (mock)")
        }
    }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
