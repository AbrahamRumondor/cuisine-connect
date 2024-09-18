package com.example.cuisineconnect.app.screen.recipe.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.step.StepUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val stepUseCase: StepUseCase
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
    recipe: Recipe?, user: User?, ingredients: List<String>?, steps: List<Step>?
  ): MutableList<Any>? {
    if (user == null || recipe == null) return null
    val detailRecipe = mutableListOf(
      "Header" to recipe,
      user,
      "Estimation" to recipe,
      "Bahan-bahan"
    )

    if (ingredients != null) {
      detailRecipe.addAll(ingredients)
    }
    detailRecipe.add("Langkah-langkah")
    if (steps != null) {
      detailRecipe.addAll(steps)
    }
    detailRecipe.add(true) // add bottom spacing

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

}