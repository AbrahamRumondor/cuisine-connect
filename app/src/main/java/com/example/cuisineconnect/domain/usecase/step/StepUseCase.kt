package com.example.cuisineconnect.domain.usecase.step

import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.model.Step
import kotlinx.coroutines.flow.StateFlow

interface StepUseCase {
  suspend fun getSteps(recipeId: String): StateFlow<List<Step>>
  fun setSteps(steps: List<Pair<String, StepResponse>>)
}
