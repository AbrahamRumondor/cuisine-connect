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
  @Named("recipesRef") private val recipesRef: CollectionReference,
  @Named("usersRef") private val usersRef: CollectionReference,
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

  override suspend fun getSteps(userId: String, recipeId: String): StateFlow<List<Step>> {
    try {
      // Reference to the user document, then to the specific recipe's steps collection
      val userDocRef = usersRef.document(userId) // usersRef is the reference to the "users" collection
      val stepsSnapshot = userDocRef.collection("recipes")
        .document(recipeId)
        .collection("steps")
        .get()
        .await()

      // Convert the Firestore documents to a list of StepResponse objects
      val stepList = stepsSnapshot.toObjects(StepResponse::class.java)

      // Transform StepResponse objects into Step objects
      _steps.value = stepList.map { StepResponse.transform(it) }
    } catch (e: Exception) {
      // In case of an error, return an empty list
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

  suspend fun replaceSteps(userId: String, recipeId: String, newSteps: List<Pair<String, StepResponse>>) {
    try {
      // Reference to the user document and the recipe's steps collection
      val userDocRef = usersRef.document(userId)
      val stepsCollectionRef = userDocRef.collection("recipes")
        .document(recipeId)
        .collection("steps")

      // Step 1: Delete all existing steps
      val stepsSnapshot = stepsCollectionRef.get().await()
      for (document in stepsSnapshot.documents) {
        document.reference.delete()
          .addOnSuccessListener {
            Timber.tag("TEST").d("Successfully deleted step: ${document.id}")
          }
          .addOnFailureListener { exception ->
            Timber.tag("TEST").e("Error deleting step ${document.id}: ${exception.message}")
          }
      }

      // Step 2: Insert the new steps
      setSteps(newSteps)

      Timber.tag("TEST").d("All steps deleted and new steps inserted successfully.")
    } catch (e: Exception) {
      Timber.tag("TEST").e("Error replacing steps for recipe $recipeId: ${e.message}")
    }
  }
}