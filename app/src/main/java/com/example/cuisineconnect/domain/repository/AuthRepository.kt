package com.example.cuisineconnect.domain.repository

import com.google.firebase.auth.AuthResult

interface AuthRepository {
  fun getCurrentUserID(): String
  suspend fun registerUser(email: String, password: String): AuthResult?
  suspend fun loginUser(email: String, password: String): AuthResult?
}