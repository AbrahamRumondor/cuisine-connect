package com.example.cuisineconnect.app.screen.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val authUseCase: AuthUseCase
) : ViewModel() {

  fun registerUser(
    email: String,
    displayName: String,
    username: String,
    password: String,
    onComplete: (Boolean, String?) -> Unit // Callback with success status and error message
  ) {
    viewModelScope.launch {
      try {
        // Register user in Firebase Authentication
        val registerResult = authUseCase.registerUser(email, password)
        if (registerResult.isSuccess) {
          val uid = registerResult.getOrThrow() // Get the UID of the created user

          // Create the User object
          val user = User(
            id = uid,
            username = username,
            email = email,
            displayName = displayName,
            password = hashPassword(password)
          )

          // Attempt to store the user in Firestore
          val storeResult = userUseCase.storeUser(uid, user, isUpdate = false)
          storeResult.onSuccess {
            onComplete(true, null) // Registration and storage succeeded
          }.onFailure { error ->
            // Handle failure in storing user, pass error message
            onComplete(false, error.message)
          }
        } else {
          // General failure in user registration
          onComplete(false, "Registration failed: Unable to create user")
        }
      } catch (e: Exception) {
        // Catch and handle any unexpected exceptions
        onComplete(false, e.message ?: "An unexpected error occurred")
      }
    }
  }

  private fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
      .digest(password.toByteArray())
      .joinToString("") { "%02x".format(it) }
  }
}
