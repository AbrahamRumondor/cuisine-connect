package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.RecipeRepository
import com.example.cuisineconnect.domain.repository.UserRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class RecipeRepositoryImpl @Inject constructor(
  @Named("recipesRef") private val recipesRef: CollectionReference,
  @Named("usersRef") private val usersRef: CollectionReference,
  private val userRepository: UserRepository
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
    return "r_${userId}_${recipesRef.document().id}"
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
        Timber.tag("RemoveUpvote")
          .d("User $userId removed upvote from recipe $recipeId successfully")
      }
    } catch (e: Exception) {
      Timber.tag("RemoveUpvote").e(e, "Error removing upvote from recipe $recipeId by user $userId")
    }
  }

  override suspend fun removeRecipe(recipeId: String) {
    try {
      val userId = recipeId.substringAfter("_").substringBefore("_")

      val user = userRepository.getUserByUserId(userId)
      if (user == null) {
        Timber.tag("RemoveRecipe").e("User with id $userId not found, aborting recipe deletion")
        return
      }

      val updatedRecipes = user.recipes.filterNot { recipe -> recipe == recipeId }
      userRepository.storeUser(userId, user.copy(recipes = updatedRecipes), isUpdate = true)

      val recipeDoc = recipesRef.document(recipeId)
      recipeDoc.delete().await()

      Timber.tag("RemoveRecipe").d("Recipe $recipeId deleted successfully")
    } catch (e: Exception) {
      Timber.tag("RemoveRecipe").e(e, "Error deleting recipe $recipeId")
    }
  }

  override suspend fun getRecipesForHome(userId: String): StateFlow<List<Pair<User, Recipe>>> {
    val _recipesFlow = MutableStateFlow<List<Pair<User, Recipe>>>(emptyList())

    val user = userRepository.getUserByUserId(userId) ?: return _recipesFlow

    val recipesList = user.recipes.map { recipeIdWithUser ->
      val authorId = recipeIdWithUser.substringAfter("_").substringBefore("_")

      coroutineScope {
        val authorDeferred = async { userRepository.getUserByUserId(authorId) }
        val recipeDeferred = async { getRecipeByID(recipeIdWithUser) }

        val author = authorDeferred.await()
        val recipe = recipeDeferred.await()

        // Return a Pair if both are non-null
        if (author != null && recipe != null) {
          Pair(author, recipe)
        } else {
          null
        }
      }
    }.filterNotNull() // Remove any null results

    _recipesFlow.value = recipesList
    return _recipesFlow
  }

  // Add these methods inside RecipeRepositoryImpl

  override suspend fun addToBookmark(
    recipeId: String,
    userId: String,
    result: (Recipe) -> Unit
  ) {
    try {
      // Update recipe to include user in bookmarks
      val recipeDoc = recipesRef.document(recipeId)
      val snapshot = recipeDoc.get().await()
      val recipeResponse = snapshot.toObject(RecipeResponse::class.java)

      recipeResponse?.let { response ->
        val bookmarks = response.bookmarks.toMutableMap()
        bookmarks[userId] = true // Add user to recipe bookmarks

        response.bookmarks = bookmarks
        recipeDoc.set(response).await()

        // Update user to include recipe in their bookmarks
        val userDoc = usersRef.document(userId)
        userDoc.update("user_bookmarks", FieldValue.arrayUnion(recipeId)).await()

        Timber.tag("Bookmark").d("User $userId bookmarked recipe $recipeId successfully")
        result(RecipeResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("Bookmark").e(e, "Error bookmarking recipe $recipeId by user $userId")
    }
  }

  override suspend fun removeFromBookmark(
    recipeId: String,
    userId: String,
    result: (Recipe) -> Unit
  ) {
    try {
      // Update recipe to remove user from bookmarks
      val recipeDoc = recipesRef.document(recipeId)
      val snapshot = recipeDoc.get().await()
      val recipeResponse = snapshot.toObject(RecipeResponse::class.java)

      recipeResponse?.let { response ->
        val bookmarks = response.bookmarks.toMutableMap()
        bookmarks.remove(userId) // Remove user from recipe bookmarks

        response.bookmarks = bookmarks
        recipeDoc.set(response).await()

        // Update user to remove recipe from their bookmarks
        val userDoc = usersRef.document(userId)
        userDoc.update("user_bookmarks", FieldValue.arrayRemove(recipeId)).await()

        Timber.tag("RemoveBookmark").d("User $userId removed bookmark from recipe $recipeId successfully")
        result(RecipeResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("RemoveBookmark").e(e, "Error removing bookmark from recipe $recipeId by user $userId")
    }
  }
}