package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
  private val auth: FirebaseAuth
) : AuthRepository {

  override fun getCurrentUserID(): String {
    return auth.currentUser?.uid ?: ""
  }

  override suspend fun registerUser(email: String, password: String): AuthResult? {
    return auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Result.success(task.result?.user?.uid ?: "")
        } else {
          Result.failure(Exception(task.exception))
        }
      }.await()
  }

  override suspend fun loginUser(email: String, password: String): AuthResult? {
    return auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Result.success(task.result?.user?.uid ?: "")
        } else {
          Result.failure(Exception(task.exception))
        }
      }.await()
  }
}