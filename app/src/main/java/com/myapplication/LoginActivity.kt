package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myapplication.databinding.ActivityLoginBinding
import com.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()

        binding = ActivityLoginBinding.inflate(layoutInflater)

//        if (auth.currentUser != null) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }afsdfdsffsd

        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.emailEditText.error = "Email tidak boleh kosong"
                binding.passwordEditText.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login berhasil
                        val user = auth.currentUser
                        // Pindah ke halaman utama atau dashboard
                        val intent = Intent(this, WargaHomeActivity::class.java)
                        intent.putExtra("USER_ID", user?.uid)
                        intent.putExtra("USER_EMAIL", user?.email)
                        startActivity(intent)
                        finish() // supaya tidak kembali ke login
                    } else {
                        // Login gagal
                        val errorMessage = task.exception?.message ?: "Login gagal"
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
