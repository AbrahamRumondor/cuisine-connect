package com.example.cuisineconnect.app.screen.authentication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cuisineconnect.app.MainActivity
import com.example.cuisineconnect.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

  private lateinit var binding: ActivityRegisterBinding
  private val registerViewModel: RegisterViewModel by viewModels()

  private lateinit var auth: FirebaseAuth

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    if (currentUser != null) {
      goToHome()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityRegisterBinding.inflate(layoutInflater)
    setContentView(binding.root)
    auth = Firebase.auth

    setRegisterButton()
    setToLoginScreen()
  }

  private fun setRegisterButton() {
    binding.run {
      btnCreate.setOnClickListener {
        pbCreate.visibility = View.VISIBLE
        btnCreate.visibility = View.GONE

        val displayName = etDisplayName.text.toString()
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        // Define regex pattern for allowed characters (alphanumeric and underscore)
        val usernamePattern = "^[a-zA-Z0-9_]+$".toRegex()

        if (displayName.isEmpty() || !displayName.matches(usernamePattern)) {
          Toast.makeText(
            this@RegisterActivity,
            "Display name must contain only letters, numbers, or underscores",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        if (username.isEmpty() || !username.matches(usernamePattern)) {
          Toast.makeText(
            this@RegisterActivity,
            "Username must contain only letters, numbers, or underscores",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        if (email.isEmpty()) {
          Toast.makeText(this@RegisterActivity, "Please enter your email", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }

        if (password.isEmpty()) {
          Toast.makeText(this@RegisterActivity, "Please enter your password", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }

        registerViewModel.registerUser(
          displayName = displayName,
          email = email,
          password = password,
          username = username
        ) { success, error ->
          if (success) {
            pbCreate.visibility = View.GONE
            btnCreate.visibility = View.VISIBLE
            Toast.makeText(
              baseContext,
              "Registration successful.",
              Toast.LENGTH_SHORT,
            ).show()
            goToLogin()
          } else {
            pbCreate.visibility = View.GONE
            btnCreate.visibility = View.VISIBLE
            Toast.makeText(
              baseContext,
              error ?: "Authentication failed.",
              Toast.LENGTH_SHORT,
            ).show()
          }
        }
      }
    }
  }

  private fun goToHome() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }

  private fun setToLoginScreen() {
    binding.tvToLogin.setOnClickListener {
      goToLogin()
    }
  }

  private fun goToLogin() {
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
  }

}