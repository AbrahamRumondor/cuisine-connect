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
    name: String,
    password: String,
    onComplete: (Boolean) -> Unit
  ) {
    viewModelScope.launch {
      authUseCase.registerUser(email, password)
      try {
        val uid = authUseCase.getCurrentUserID()
        val user = User(
          id = uid,
          email = email,
          name = name,
          password = hashPassword(password)
        )
        userUseCase.storeUser(uid, user)
        onComplete(true)
      } catch (
        e: Exception
      ) {
        onComplete(false)
      }
    }
  }

  private fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
      .digest(password.toByteArray())
      .joinToString("") { "%02x".format(it) }
  }
}
