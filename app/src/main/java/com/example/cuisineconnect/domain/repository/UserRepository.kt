package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
  suspend fun getCurrentUser(uid: String): StateFlow<User>
  suspend fun getUserByUserId(userId: String): User?
  suspend fun storeUser(uid: String, user: User, isUpdate: Boolean = false): Result<Unit>
  fun addRecipeToUser(newRecipe: String)
  fun addPostToUser(newPost: String)
  suspend fun getUsersStartingWith(prefix: String): List<User>
  fun followUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
  fun unfollowUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
  suspend fun getUserBookmarks(userId: String): StateFlow<List<String>>
}