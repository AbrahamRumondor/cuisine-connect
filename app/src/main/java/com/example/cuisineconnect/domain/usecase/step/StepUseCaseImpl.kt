package com.example.cuisineconnect.domain.usecase.step

import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.repository.StepRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class StepUseCaseImpl @Inject constructor(
  private val stepRepository: StepRepository
) : StepUseCase {

  override suspend fun getSteps(recipeId: String): StateFlow<List<Step>> {
    return stepRepository.getSteps(recipeId)
  }

  override fun setSteps(steps: List<Pair<String, StepResponse>>) {
    stepRepository.setSteps(steps)
  }

}