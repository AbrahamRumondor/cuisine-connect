package com.example.cuisineconnect.domain.usecase.recipe

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import kotlinx.coroutines.flow.StateFlow

interface RecipeUseCase {
    suspend fun getRecipes(): StateFlow<List<Recipe>>
    suspend fun getMyRecipes(userId: String): StateFlow<List<Recipe>>
    fun getRecipeDocID(): String
    fun setRecipe(recipeId: String, recipeResponse: RecipeResponse)
    suspend fun getRecipeByID(recipeId: String): Recipe?
}