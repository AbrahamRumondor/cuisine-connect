package com.example.cuisineconnect.domain.usecase.user

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserUseCase {
  suspend fun getCurrentUser(): StateFlow<User>
  suspend fun getUserByUserId(userId: String): User?
  suspend fun storeUser(uid: String, user: User, isUpdate: Boolean = false): Result<Unit>
  fun addRecipeToUser(newRecipe: String)
  fun addPostToUser(newPost: String)
  suspend fun getUsersStartingWith(prefix: String): List<User>
  fun followUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
  fun unfollowUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback)
  suspend fun getUserBookmarks(userId: String): StateFlow<List<String>>
  fun savePostContentForCurrentUser(
    postContent: List<Map<String, String>>,
    callback: TwoWayCallback
  )

  fun clearPostContentForCurrentUser(callback: TwoWayCallback)
  fun fetchPostContentForCurrentUser(callback: (result: Result<MutableList<MutableMap<String, String>>>) -> Unit)
  fun saveRecipeContentForCurrentUser(recipeId: String, recipeResponse: RecipeResponse, callback: TwoWayCallback)
  fun clearRecipeContentForCurrentUser(callback: TwoWayCallback)
  fun fetchRecipeContentForCurrentUser(callback: (Map<String, Any>?) -> Unit)
}
