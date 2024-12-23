package com.example.cuisineconnect.domain.usecase.recipe

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface RecipeUseCase {
  suspend fun getRecipes(): StateFlow<List<Recipe>>
  suspend fun getMyRecipes(userId: String): StateFlow<List<Recipe>>
  fun getRecipeDocID(userId: String): String
  fun getRecipeStepDocID(recipeId: String): String
  fun setRecipe(recipeId: String, recipeResponse: RecipeResponse)
  suspend fun getRecipeByID(recipeId: String): Recipe?
  suspend fun upvoteRecipe(recipeId: String, userId: String)
  suspend fun removeUpvote(recipeId: String, userId: String)
  suspend fun removeRecipe(recipeId: String)
  suspend fun getRecipesForHome(userId: String): StateFlow<List<Pair<User, Recipe>>>
  suspend fun addToBookmark(
    recipeId: String,
    userId: String,
    result: (Recipe) -> Unit
  )

  suspend fun removeFromBookmark(
    recipeId: String,
    userId: String,
    result: (Recipe) -> Unit
  )
}