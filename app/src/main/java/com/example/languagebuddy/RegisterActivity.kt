package com.example.languagebuddy

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val first = findViewById<EditText>(R.id.etFirstName)
        val last  = findViewById<EditText>(R.id.etLastName)
        val dob   = findViewById<EditText>(R.id.etDob)
        val email = findViewById<EditText>(R.id.etEmail)
        val pass  = findViewById<EditText>(R.id.etPassword)
        val btn   = findViewById<Button>(R.id.btnSubmit)

        btn.setOnClickListener {
            val f = first.text.toString().trim()
            val l = last.text.toString().trim()
            val d = dob.text.toString().trim()
            val e = email.text.toString().trim()
            val p = pass.text.toString()

            fun err(m:String){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show() }

            if (f.isEmpty() || l.isEmpty() || d.isEmpty() || e.isEmpty() || p.isEmpty()) { err("All fields are required"); return@setOnClickListener }
            if (f.length !in 3..30) { err("First name must be 3–30 chars"); return@setOnClickListener }
            if (l.length !in 2..30) { err("Family name must be 2–30 chars"); return@setOnClickListener }
            if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(d)) { err("DOB must be YYYY-MM-DD"); return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { err("Invalid email"); return@setOnClickListener }
            if (p.length < 6) { err("Password must be at least 6 chars"); return@setOnClickListener }

            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            finish() // back to AuthChoice
        }
    }
}
