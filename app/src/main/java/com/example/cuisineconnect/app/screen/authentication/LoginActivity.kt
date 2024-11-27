package com.example.cuisineconnect.app.screen.authentication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivity
import com.example.cuisineconnect.databinding.ActivityLoginBinding
import com.example.cuisineconnect.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding

  private lateinit var auth: FirebaseAuth

  public override fun onStart() {
    super.onStart()
    // Check if user is signed in (non-null) and update UI accordingly.
    val currentUser = auth.currentUser
    if (currentUser != null && !currentUser.email.isNullOrEmpty()) {
      Log.d("loginActivity", "${currentUser.email}")
      goToHome()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)
    auth = Firebase.auth

    setLoginButton()
    setToRegisterScreen()
  }

  private fun setLoginButton() {
    binding.run {
      btnLogin.setOnClickListener {
        pbCreate.visibility = View.VISIBLE
        btnLogin.visibility = View.GONE

        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        // Email field validation
        if (email.isEmpty()) {
          Toast.makeText(this@LoginActivity, "Please enter your email", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

        if (!email.contains("@")) {
          Toast.makeText(this@LoginActivity, "Email must contain '@' symbol", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

        if (!email.contains(".")) {
          Toast.makeText(this@LoginActivity, "Email must contain a domain (e.g., '.com')", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          Toast.makeText(this@LoginActivity, "Please enter a valid email ex:'example@gmail.com'", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

// Password field validation
        if (password.isEmpty()) {
          Toast.makeText(this@LoginActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

        if (password.length < 5) {
          Toast.makeText(this@LoginActivity, "Password must be more than 4 characters", Toast.LENGTH_SHORT).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

// Password complexity validation (optional)
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{5,}\$")
        if (!passwordRegex.matches(password)) {
          Toast.makeText(
            this@LoginActivity,
            "Password must contain at least one uppercase letter, one lowercase letter, and one number",
            Toast.LENGTH_SHORT
          ).show()
          pbCreate.visibility = View.GONE
          btnLogin.visibility = View.VISIBLE
          return@setOnClickListener
        }

        auth.signInWithEmailAndPassword(email, password)
          .addOnCompleteListener { task ->
            pbCreate.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE

            if (task.isSuccessful) {
              // Sign in success, update UI with the signed-in user's information
              Log.d(TAG, "signInWithEmail:success")
              Toast.makeText(this@LoginActivity, "Welcome!", Toast.LENGTH_SHORT).show()

              // TODO create account in firestore

              goToHome()
            } else {
              // If sign in fails, display a message to the user.
              Log.w(TAG, "signInWithEmail:failure", task.exception)
              Toast.makeText(
                baseContext,
                "Authentication failed.",
                Toast.LENGTH_SHORT,
              ).show()
            }
          }.addOnFailureListener {
            pbCreate.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE
            Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
          }
      }
    }
  }

  private fun goToHome() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }

  private fun setToRegisterScreen() {
    binding.tvToRegister.setOnClickListener {
      goToRegister()
    }
  }

  private fun goToRegister() {
    val intent = Intent(this, RegisterActivity::class.java)
    startActivity(intent)
  }

}