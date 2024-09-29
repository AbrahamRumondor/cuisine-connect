package com.example.cuisineconnect.domain.repository

import com.google.firebase.auth.AuthResult

interface AuthRepository {
  fun getCurrentUserID(): String
  suspend fun registerUser(email: String, password: String): AuthResult?
  suspend fun loginUser(email: String, password: String): AuthResult?
  suspend fun updateEmail(newEmail: String): Result<Unit>
  suspend fun updatePassword(newPassword: String): Result<Unit>
  suspend fun reAuthenticateUser(email: String, password: String): Result<Unit>
}