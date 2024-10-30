package com.example.cuisineconnect.app.screen.recipe.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.hashtag.HashtagUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.step.StepUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val stepUseCase: StepUseCase,
  private val hashtagUseCase: HashtagUseCase
) : ViewModel() {

  private val _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
  val currentUser: StateFlow<User?> = _currentUser

  private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
  val user: StateFlow<User?> = _user

  private val _recipe: MutableStateFlow<Recipe?> = MutableStateFlow(null)
  val recipe: StateFlow<Recipe?> = _recipe

  private val _steps: MutableStateFlow<List<Step>?> = MutableStateFlow(null)
  val steps: StateFlow<List<Step>?> = _steps

  init {
    fetchCurrentUser()
  }

  fun fetchCurrentUser() {
    viewModelScope.launch {
      try {
        userUseCase.getCurrentUser().collectLatest {
          _currentUser.value = it
        }
      } catch (e: Exception) {
        Timber.tag("USER_PROBLEM").e("Error fetching user: %s", e.message)
      }
    }
  }

  fun fetchUser(userId: String) {
    viewModelScope.launch {
      try {
        _user.value = userUseCase.getUserByUserId(userId)
      } catch (e: Exception) {
        Timber.tag("USER_PROBLEM").e("Error fetching user: %s", e.message)
      }
    }
  }

  fun fetchRecipe(recipeId: String) {
    viewModelScope.launch {
      try {
        _recipe.value = recipeUseCase.getRecipeByID(recipeId)
      } catch (e: Exception) {
        Timber.tag("USER_PROBLEM").e("Error fetching user: %s", e.message)
      }
    }
  }

  fun fetchSteps(recipeId: String) {
    viewModelScope.launch {
      try {
        val steps = stepUseCase.getSteps(recipeId).value
        _steps.value = steps.sortedBy { it.no }
      } catch (e: Exception) {
        Timber.tag("USER_PROBLEM").e("Error fetching steps: %s", e.message)
      }
    }
  }

  fun makeDetailRecipe(
    recipe: Recipe?,
    user: User?,
    ingredients: List<String>?,
    steps: List<Step>?,
  ): MutableList<Any>? {
    if (user == null || recipe == null) return null

    val detailRecipe = mutableListOf<Any>(
      "Header" to recipe,
      user,
      "Estimation" to recipe,
      "Bahan-bahan"
    )

    // Add ingredients if available
    if (ingredients != null) {
      detailRecipe.addAll(ingredients)
    }

    detailRecipe.add("Langkah-langkah")

    // Add steps if available
    if (steps != null) {
      detailRecipe.addAll(steps)
    }

    if (recipe.hashtags.isNotEmpty()) {
      detailRecipe.add(recipe.hashtags)
    }

    detailRecipe.add(true) // Add bottom spacing

    return detailRecipe
  }

  fun upvoteRecipe(recipeId: String, userId: String) {
    viewModelScope.launch {
      recipeUseCase.upvoteRecipe(recipeId, userId)
      fetchRecipe(recipeId)
    }
  }

  fun downVoteRecipe(recipeId: String, userId: String) {
    viewModelScope.launch {
      recipeUseCase.removeUpvote(recipeId, userId)
      fetchRecipe(recipeId)
    }
  }

  fun addToBookmark(
    recipeId: String,
    userId: String
  ) {
    viewModelScope.launch {
      recipeUseCase.addToBookmark(recipeId, userId) {
        fetchRecipe(recipeId)
      }
    }
  }

  fun removeFromBookmark(
    recipeId: String,
    userId: String,
  ) {
    viewModelScope.launch {
      recipeUseCase.removeFromBookmark(recipeId, userId) {
        fetchRecipe(recipeId)
      }
    }
  }

  fun updateTrendingCounter(hashtagsBody: List<String>, itemId: String) {
    if (hashtagsBody.isEmpty()) return

    viewModelScope.launch {
      val newTimestamp = Date().time
      val increment = 1
      hashtagsBody.forEach { hashtagBody ->
        try {
          hashtagUseCase.updateHashtagWithScore(hashtagBody, itemId, newTimestamp, increment)
        } catch (e: Exception) {
          Timber.tag("HASHTAG_UPDATE_ERROR").e("Failed to update hashtag $hashtagBody: ${e.message}")
        }
      }
    }
  }

}