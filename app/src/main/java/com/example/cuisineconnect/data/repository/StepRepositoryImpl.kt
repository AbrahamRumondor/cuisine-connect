package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.repository.StepRepository
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class StepRepositoryImpl @Inject constructor(
  @Named("stepsRef") private val stepsRef: CollectionReference,
  @Named("recipesRef") private val recipesRef: CollectionReference
) : StepRepository {

  private val _steps = MutableStateFlow<List<Step>>(emptyList())
  private val steps: StateFlow<List<Step>> = _steps

  override suspend fun getSteps(recipeId: String): StateFlow<List<Step>> {
    try {
      val recipeDocRef = recipesRef.document(recipeId)
      val stepsSnapshot = recipeDocRef.collection("steps").get().await()

      // Convert the Firestore documents to a list of StepResponse objects
      val stepList = stepsSnapshot.toObjects(StepResponse::class.java)

      // Transform StepResponse objects into Step objects
      _steps.value = stepList.map { StepResponse.transform(it) }
    } catch (e: Exception) {
      _steps.value = emptyList()
    }
    return steps
  }

  override fun setSteps(steps: List<Pair<String, StepResponse>>) {
    for ((stepId, stepResponse) in steps) {
      stepsRef.document(stepId).set(stepResponse)
        .addOnSuccessListener {
          Timber.tag("TEST").d("SUCCESS ON step INSERTION for stepId: $stepId")
        }
        .addOnFailureListener { exception ->
          Timber.tag("TEST")
            .d("ERROR ON step INSERTION for stepId: $stepId. Error: ${exception.message}")
        }
    }
  }
}