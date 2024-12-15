package com.example.cuisineconnect.app.screen.authentication

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    setPasswordVisibility()
  }

  private fun setPasswordVisibility() {
    // Get reference to the password EditText and TextInputLayout
    val passwordEditText = binding.etPassword
    val textInputLayout = binding.tilPassword // Ensure you use the correct ID for TextInputLayout

    // Keep track of the visibility state
    var isPasswordVisible = false

    // Set an OnClickListener for the end icon
    textInputLayout.setEndIconOnClickListener {
      // Toggle password visibility
      isPasswordVisible = !isPasswordVisible

      // Update input type and cursor position
      passwordEditText.inputType = if (isPasswordVisible) {
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
      } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
      }
      passwordEditText.setSelection(passwordEditText.text?.length ?: 0)

      // Update the end icon drawable
      textInputLayout.endIconDrawable = if (isPasswordVisible) {
        ContextCompat.getDrawable(this, R.drawable.ic_eye_closed) // Replace with your "eye closed" icon
      } else {
        ContextCompat.getDrawable(this, R.drawable.ic_eye_open) // Replace with your "eye open" icon
      }
    }
  }

  private fun setLoginButton() {
    binding.run {
      btnLogin.setOnClickListener {
        closeKeyboard()

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

  private fun closeKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusView = this.currentFocus
    if (currentFocusView != null) {
      imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
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