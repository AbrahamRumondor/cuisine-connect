package com.example.cuisineconnect.app.screen.profile.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherProfileRecipeViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Recipe?>>?> = MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Recipe?>>?> = _list

  private val _otherUser: MutableStateFlow<User> = MutableStateFlow(User())
  val otherUser: StateFlow<User> = _otherUser

  fun getUser(userId: String) {
    viewModelScope.launch {
      val user = userUseCase.getUserByUserId(userId)
      user?.let {
        _otherUser.value = it
        getRecipesOfUser()
      }
    }
  }

  fun getRecipesOfUser() {
    viewModelScope.launch {
      _otherUser.collectLatest { user ->
        // Fetch recipes asynchronously
        val recipesDeferred = user.recipes.map { recipeId ->
          async {
            val recipe = recipeUseCase.getRecipeByID(recipeId)
            Pair(user, recipe)
          }
        }

        // Await all recipes to complete
        val recipes = recipesDeferred.awaitAll()

        // Sort the recipe list by date in descending order
        val sortedList = recipes
          .filter { pair ->
            pair.second is Recipe
          }
          .sortedByDescending { pair ->
            (pair.second as Recipe).date.time // Assuming Recipe has a 'date' property of type Date
          }

        // Update the StateFlow with the sorted list
        _list.value = sortedList
      }
    }
  }
}