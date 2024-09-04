package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import kotlinx.coroutines.flow.StateFlow

interface RecipeRepository {
    suspend fun getRecipes(): StateFlow<List<Recipe>>
    suspend fun getMyRecipes(userId: String): StateFlow<List<Recipe>>
    fun getRecipeDocID(): String
    fun setRecipe(recipeId: String, recipeResponse: RecipeResponse)
    suspend fun getRecipeByID(recipeId: String): Recipe?
}