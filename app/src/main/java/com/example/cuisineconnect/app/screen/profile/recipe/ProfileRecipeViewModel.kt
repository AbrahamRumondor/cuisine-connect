package com.example.cuisineconnect.app.screen.profile.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
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
class ProfileRecipeViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Recipe?>>?> =
    MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Recipe?>>?> = _list

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
      getRecipeOfUser()
    }
  }

  fun getRecipeOfUser() {
    viewModelScope.launch {
      user.collectLatest { user ->
        // Fetch recipes asynchronously
        val recipesDeferred = user.recipes.map { recipeId ->
          async {
            _user.value = userUseCase.getCurrentUser().value
            currentUser = _user.value
            val recipe = recipeUseCase.getRecipeByID(recipeId)
            Pair(user, recipe) // Pair with user
          }
        }

        // Await all recipes to complete
        val recipes = recipesDeferred.awaitAll()

        // Sort the recipes by date in descending order
        val sortedRecipes = recipes
          .filter { pair ->
            pair.second is Recipe
          }
          .sortedByDescending { pair ->
            (pair.second as? Recipe)?.date?.time ?: 0L
          }

        // Update the StateFlow with the sorted recipes list
        _list.value = sortedRecipes
      }
    }
  }

  fun deleteRecipe(recipeId: String) {
    viewModelScope.launch {
      recipeUseCase.removeRecipe(recipeId)
    }
  }

}