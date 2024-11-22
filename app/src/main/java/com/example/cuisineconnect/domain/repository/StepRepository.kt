package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import kotlinx.coroutines.flow.StateFlow

interface StepRepository {
  suspend fun getSteps(recipeId: String): StateFlow<List<Step>>
  fun setSteps(steps: List<Pair<String, StepResponse>>)
  suspend fun getSteps(userId: String, recipeId: String): StateFlow<List<Step>>
}
