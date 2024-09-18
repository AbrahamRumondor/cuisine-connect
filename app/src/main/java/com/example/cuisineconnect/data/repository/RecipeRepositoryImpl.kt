package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.repository.RecipeRepository
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class RecipeRepositoryImpl @Inject constructor(
  @Named("recipesRef") private val recipesRef: CollectionReference
) : RecipeRepository {

  private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
  private val recipes: StateFlow<List<Recipe>> = _recipes

  override suspend fun getRecipes(): StateFlow<List<Recipe>> {
    try {
      val snapshot = recipesRef.get().await()
      val recipeList = snapshot.toObjects(RecipeResponse::class.java)
      _recipes.value = recipeList.map { RecipeResponse.transform(it) }
    } catch (e: Exception) {
      _recipes.value = emptyList()
    }
    return recipes
  }

  override suspend fun getMyRecipes(userId: String): StateFlow<List<Recipe>> {
    try {
      val snapshot = recipesRef.get().await()
      val recipeList =
        snapshot.toObjects(RecipeResponse::class.java).filter { it.id.contains(userId) }
      _recipes.value = recipeList.map { RecipeResponse.transform(it) }
    } catch (e: Exception) {
      _recipes.value = emptyList()
    }
    return recipes
  }

  override fun getRecipeDocID(userId: String): String {
    return "${userId}_${recipesRef.document().id}"
  }

  override fun getRecipeStepDocID(recipeId: String): String {
    return recipesRef.document(recipeId).collection("steps").document().id
  }

  override fun setRecipe(recipeId: String, recipeResponse: RecipeResponse) {
    recipesRef.document(recipeId).set(recipeResponse).addOnSuccessListener {
      Timber.tag("TEST").d("SUCCESS ON recipe INSERTION")
    }
      .addOnFailureListener { Timber.tag("TEST").d("ERROR ON recipe INSERTION") }
  }

  override suspend fun getRecipeByID(recipeId: String): Recipe? {
    return try {
      val snapshot = recipesRef.document(recipeId).get().await()
      val recipe =
        snapshot.toObject(RecipeResponse::class.java)?.let { RecipeResponse.transform(it) }

      recipe
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun upvoteRecipe(recipeId: String, userId: String) {
    try {
      val recipeDoc = recipesRef.document(recipeId)
      val snapshot = recipeDoc.get().await()
      val recipeResponse = snapshot.toObject(RecipeResponse::class.java)

      recipeResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()
        upvotes[userId] = true // Mark the user's upvote as true

        response.upvotes = upvotes
        recipeDoc.set(response).await()
        Timber.tag("Upvote").d("User $userId upvoted recipe $recipeId successfully")
      }
    } catch (e: Exception) {
      Timber.tag("Upvote").e(e, "Error upvoting recipe $recipeId by user $userId")
    }
  }

  // New method to remove an upvote
  override suspend fun removeUpvote(recipeId: String, userId: String) {
    try {
      val recipeDoc = recipesRef.document(recipeId)
      val snapshot = recipeDoc.get().await()
      val recipeResponse = snapshot.toObject(RecipeResponse::class.java)

      recipeResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()

        response.upvotes = upvotes.filterNot { it.key == userId }
        recipeDoc.set(response).await()
        Timber.tag("RemoveUpvote").d("User $userId removed upvote from recipe $recipeId successfully")
      }
    } catch (e: Exception) {
      Timber.tag("RemoveUpvote").e(e, "Error removing upvote from recipe $recipeId by user $userId")
    }
  }

}