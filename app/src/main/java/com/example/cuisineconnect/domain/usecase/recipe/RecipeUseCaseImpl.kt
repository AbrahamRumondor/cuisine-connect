package com.example.cuisineconnect.domain.usecase.recipe

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class RecipeUseCaseImpl @Inject constructor(
  private val recipeRepository: RecipeRepository
) : RecipeUseCase {

  override suspend fun getRecipes(): StateFlow<List<Recipe>> {
    return recipeRepository.getRecipes()
  }

  override suspend fun getMyRecipes(userId: String): StateFlow<List<Recipe>> {
    return recipeRepository.getMyRecipes(userId)
  }

  override fun getRecipeDocID(userId: String): String {
    return recipeRepository.getRecipeDocID(userId)
  }

  override fun getRecipeStepDocID(recipeId: String): String {
    return recipeRepository.getRecipeStepDocID(recipeId)
  }

  override fun setRecipe(recipeId: String, recipeResponse: RecipeResponse) {
    recipeRepository.setRecipe(recipeId, recipeResponse)
  }

  override suspend fun getRecipeByID(recipeId: String): Recipe? {
    return recipeRepository.getRecipeByID(recipeId)
  }

  override suspend fun upvoteRecipe(recipeId: String, userId: String) {
    recipeRepository.upvoteRecipe(recipeId, userId)
  }

  override suspend fun removeUpvote(recipeId: String, userId: String) {
    recipeRepository.removeUpvote(recipeId, userId)
  }
}