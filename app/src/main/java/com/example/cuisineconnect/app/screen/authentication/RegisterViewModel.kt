package com.example.cuisineconnect.app.screen.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
      // First check if the username is taken
      userUseCase.isUsernameTaken(username,
        onSuccess = { isTaken ->
          if (isTaken) {
            // If username is taken, return the error message
            onComplete(false, "Username is already taken. Please choose another.")
          } else {
            viewModelScope.launch {
              // If username is not taken, proceed with registration
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
                    password = hashPassword(password) // Assuming you have a function to hash passwords
                  )

                  // Attempt to store the user in Firestore
                  val storeResult = userUseCase.storeUser(uid, user, isUpdate = false)
                  storeResult.onSuccess {
                    onComplete(true, null) // Registration and storage succeeded
                  }.onFailure { error ->
                    // Handle failure in storing user, pass error message
                    Log.e("RegisterUser", "Error storing user in Firestore: ${error.message}")
                    onComplete(false, "Failed to save user data. Please try again.")
                  }
                } else {
                  // General failure in user registration
                  val exception = registerResult.exceptionOrNull()
                  Log.e("RegisterUser", "Registration failed: ${exception?.message}")
                  onComplete(false, getFirebaseErrorMessage(exception))
                }
              } catch (e: Exception) {
                // Catch and handle any unexpected exceptions
                Log.e("RegisterUser", "Unexpected error: ${e.message}")
                onComplete(false, e.message ?: "An unexpected error occurred")
              }
            }
          }
        },
        onFailure = { error ->
          // Handle failure in checking if username is taken
          Log.e("RegisterUser", "Error checking username availability: ${error.message}")
          onComplete(false, "Error checking username availability. Please try again.")
        }
      )
  }

  private fun getFirebaseErrorMessage(exception: Throwable?): String {
    return when (exception) {
      is FirebaseAuthUserCollisionException -> "This email is already registered."
      is FirebaseAuthWeakPasswordException -> "Your password is too weak. Please choose a stronger password."
      is FirebaseAuthInvalidCredentialsException -> "Invalid email format. Please enter a valid email."
      is FirebaseAuthInvalidUserException -> "This account doesn't exist or has been disabled."
      else -> "An unknown error occurred. Please try again later."
    }
  }
  private fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
      .digest(password.toByteArray())
      .joinToString("") { "%02x".format(it) }
  }
}
