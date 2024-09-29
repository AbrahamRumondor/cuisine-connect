package com.example.cuisineconnect.data.repository

import android.util.Log
import com.example.cuisineconnect.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
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

  override suspend fun updateEmail(newEmail: String): Result<Unit> {
    return try {
      auth.currentUser?.updateEmail(newEmail)?.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.d("AuthRepo", "nih email: ${e}")
      Result.failure(e)
    }
  }

  override suspend fun updatePassword(newPassword: String): Result<Unit> {
    return try {
      auth.currentUser?.updatePassword(newPassword)?.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.d("AuthRepo", "nih password: ${e}")
      Result.failure(e)
    }
  }

  override suspend fun reAuthenticateUser(email: String, password: String): Result<Unit> {
    return try {
      val user = auth.currentUser
      val credential = EmailAuthProvider.getCredential(email, password)

      user?.reauthenticate(credential)?.await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e("AuthRepo", "Re-authentication failed: ${e.message}")
      Result.failure(e)
    }
  }

}