package com.example.cuisineconnect.app.screen.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cuisineconnect.R
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
        ContextCompat.getDrawable(
          this,
          R.drawable.ic_eye_closed
        ) // Replace with your "eye closed" icon
      } else {
        ContextCompat.getDrawable(this, R.drawable.ic_eye_open) // Replace with your "eye open" icon
      }
    }
  }

  private fun setRegisterButton() {
    binding.run {
      btnCreate.setOnClickListener {
        closeKeyboard()

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
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.please_enter_your_display_name),
            Toast.LENGTH_SHORT
          )
            .show()
          return@setOnClickListener
        }
        // Display Name Validation
        if (!displayName.matches(usernamePattern)) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.display_must_only_letters_numbers_or_underscores),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        if (username.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity,
            getString(R.string.please_enter_your_username), Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        // Username Validation
        if (!username.matches(usernamePattern)) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.username_must_only_letters_numbers_or_underscores),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (username.length < 3 || username.length > 15) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.username_must_be_between_3_and_15_characters),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        // Email Validation
        if (email.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity,
            getString(R.string.please_enter_your_email), Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.please_enter_a_valid_email),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }

        // Password Validation
        if (password.isEmpty()) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(this@RegisterActivity, getString(R.string.please_enter_your_password), Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }
        if (password.length < 6) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.password_must_be_at_least_6_characters),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[A-Z].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.password_must_contain_at_least_one_uppercase_letter),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[a-z].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.password_must_contain_at_least_one_lowercase_letter),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*\\d.*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.password_must_contain_at_least_one_number),
            Toast.LENGTH_SHORT
          ).show()
          return@setOnClickListener
        }
        if (!password.matches(".*[@#\$%^&+=!].*".toRegex())) {
          pbCreate.visibility = View.GONE
          btnCreate.visibility = View.VISIBLE
          Toast.makeText(
            this@RegisterActivity,
            getString(R.string.password_must_contain_at_least_one_special_character),
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
            getString(R.string.passwords_do_not_match),
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
              getString(R.string.registration_successful),
              Toast.LENGTH_SHORT,
            ).show()
            goToLogin()
          } else {
            Toast.makeText(
              baseContext,
              error ?: getString(R.string.authentication_failed),
              Toast.LENGTH_SHORT,
            ).show()
          }
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