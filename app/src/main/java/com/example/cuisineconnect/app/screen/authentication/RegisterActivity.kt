package com.example.cuisineconnect.app.screen.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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

        val displayName = etDisplayName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Define regex pattern for allowed characters (alphanumeric and underscore)
        val usernamePattern = "^[a-zA-Z0-9_]+$".toRegex()

        if (displayName.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity, "Please enter your display name", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        // Display Name Validation
        if (!displayName.matches(usernamePattern)) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "display must only letters, numbers, or underscores",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        if (username.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity, "Please enter your username", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        // Username Validation
        if (!username.matches(usernamePattern)) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "username must only letters, numbers, or underscores",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (username.length < 3 || username.length > 15) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Username must be between 3 and 15 characters",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        // Email Validation
        if (email.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity, "Please enter your email", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Please enter a valid email address",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        // Password Validation
        if (password.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity, "Please enter your password", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        if (password.length < 6) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Password must be at least 6 characters",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[A-Z].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Password must contain at least one uppercase letter",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[a-z].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Password must contain at least one lowercase letter",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*\\d.*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Password must contain at least one number",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[@#\$%^&+=!].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Password must contain at least one special character",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        val confirmPassword = etPasswordComparator.text?.toString()?.trim() ?: ""
        if (password != confirmPassword) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            "Passwords do not match",
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        // Proceed with registration
        registerViewModel.registerUser(
          displayName = displayName,
          email = email,
          password = password,
          username = username
        ) { success, error ->
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          if (success) {
            Toast.makeText(
              baseContext,
              "Registration successful.",
              Toast.LENGTH_SHORT,
            ).show()
            goToLogin()
          } else {
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