package com.example.cuisineconnect.domain.usecase.auth


import com.google.firebase.auth.AuthResult

interface AuthUseCase {
  fun getCurrentUserID(): String

  suspend fun registerUser(email: String, password: String): Result<String>
  suspend fun loginUser(email: String, password: String): AuthResult?

  suspend fun updateEmail(newEmail: String): Result<Unit>
  suspend fun updatePassword(newPassword: String): Result<Unit>
  suspend fun reAuthenticateUser(email: String, password: String): Result<Unit>
}