package com.example.cuisineconnect.domain.usecase.user

import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserUseCase {
  suspend fun getCurrentUser(): StateFlow<User>
  suspend fun getUserByUserId(userId: String): User?
  suspend fun storeUser(uid: String, user: User)
  fun addRecipeToUser(newRecipe: String)
  fun addPostToUser(newPost: String)
  suspend fun getUsersStartingWith(prefix: String): List<User>
  fun followUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
  fun unfollowUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
}