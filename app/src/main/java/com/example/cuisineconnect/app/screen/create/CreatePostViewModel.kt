package com.example.cuisineconnect.app.screen.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase
) : ViewModel() {
  private val _recipes: MutableStateFlow<Pair<User?, Recipe>?> = MutableStateFlow(null)
  val recipes: StateFlow<Pair<User?, Recipe>?> = _recipes

  val postContent: MutableList<Map<String, String>> = mutableListOf()

  fun getRecipeById(recipeId: String) {
    viewModelScope.launch {
      // Fetch the recipe
      val recipe = withContext(Dispatchers.IO) {
        recipeUseCase.getRecipeByID(recipeId)
      }

      // If recipe is not null, fetch the user
      val user = recipe?.let {
        val userId = it.id.substringBefore("_")
        withContext(Dispatchers.IO) {
          userUseCase.getUserByUserId(userId)
        }
      }

      // Set the value of _recipes if both recipe and user are available
      recipe?.let { r ->
        user?.let { u ->
          _recipes.value = Pair(u, r)
        }
      }
    }
  }

}