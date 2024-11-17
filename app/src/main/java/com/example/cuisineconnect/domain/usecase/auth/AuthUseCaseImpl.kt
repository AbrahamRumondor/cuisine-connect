package com.example.cuisineconnect.domain.usecase.auth

import com.example.cuisineconnect.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import javax.inject.Inject

class AuthUseCaseImpl @Inject constructor(
  private val authRepository: AuthRepository
) : AuthUseCase {

  override fun getCurrentUserID(): String {
    return authRepository.getCurrentUserID()
  }

  override suspend fun registerUser(email: String, password: String): Result<String>  {
    return authRepository.registerUser(email, password)
  }

  override suspend fun loginUser(email: String, password: String): AuthResult? {
    return authRepository.loginUser(email, password)
  }

  override suspend fun updateEmail(newEmail: String): Result<Unit> {
    return authRepository.updateEmail(newEmail)
  }

  override suspend fun updatePassword(newPassword: String): Result<Unit> {
    return authRepository.updatePassword(newPassword)
  }

  override suspend fun reAuthenticateUser(email: String, password: String): Result<Unit> {
    return authRepository.reAuthenticateUser(email, password)
  }
}