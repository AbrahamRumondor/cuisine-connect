package com.example.cuisineconnect.domain.usecase.auth


import com.google.firebase.auth.AuthResult

interface AuthUseCase {
  fun getCurrentUserID(): String

  suspend fun registerUser(email: String, password: String): AuthResult?
  suspend fun loginUser(email: String, password: String): AuthResult?
}