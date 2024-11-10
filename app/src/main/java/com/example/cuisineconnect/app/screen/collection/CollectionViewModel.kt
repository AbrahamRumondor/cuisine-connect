package com.example.cuisineconnect.app.screen.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase
) : ViewModel() {

  private val _recipes: MutableStateFlow<List<Pair<User?, Recipe>>?> = MutableStateFlow(null)
  val recipes: StateFlow<List<Pair<User?, Recipe>>?> = _recipes

  private val _myRecipes: MutableStateFlow<List<Pair<User?, Recipe>>> = MutableStateFlow(emptyList())
  val myRecipes: StateFlow<List<Pair<User?, Recipe>>> = _myRecipes

  private val _bookmarkedRecipes: MutableStateFlow<List<Pair<User?, Recipe>>> =
    MutableStateFlow(emptyList())
  val bookmarkedRecipes: StateFlow<List<Pair<User?, Recipe>>> = _bookmarkedRecipes

  private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
  val user: StateFlow<User?> = _user

  init {
    getRecipes()
  }

  fun getRecipes() {
    viewModelScope.launch {
      userUseCase.getCurrentUser().collectLatest { currentUser ->
        // Use coroutineScope to launch parallel tasks
        coroutineScope {
          val recipes = currentUser.recipes.mapNotNull { recipeId ->
            Log.d("brobruh", recipeId)
            async {
              recipeUseCase.getRecipeByID(recipeId) // Fetch recipe
            }
          }.awaitAll().filterNotNull() // Filter out null recipes

          // Create a pair list of User and Recipe asynchronously
          val pairList = recipes.map { recipe ->
            val userId = recipe.id.substringAfter("_").substringBefore("_")
            async {
              val user = userUseCase.getUserByUserId(userId)
              Pair(user, recipe) // Create the Pair asynchronously
            }
          }.awaitAll() // Await all user fetching tasks

          // Sort the pair list by recipe date in descending order
          val sortedPairList = pairList.sortedByDescending { pair ->
            (pair.second as? Recipe)?.date?.time ?: 0L // Use the date property for sorting
          }

          Log.d("brobruh", sortedPairList.toString())
          _recipes.value = sortedPairList // Assign the result to _myRecipes
        }
      }
    }
  }

  fun getMyRecipes() {
    getRecipes()
    viewModelScope.launch {
      recipes.collectLatest { allRecipes ->
        userUseCase.getCurrentUser().collectLatest { currentUser ->
          allRecipes?.let { recipes ->
            _myRecipes.value = recipes.filter { it.first?.id == currentUser.id }.ifEmpty { emptyList() }
          }
          Log.d("brobruh", "tes: ${_myRecipes.value.toString()}")
        }
      }
    }
  }

  fun getBookmarkedRecipes() {
    viewModelScope.launch {
      userUseCase.getCurrentUser().collectLatest { currentUser ->
        Log.d("collectionViewModel", "njir: ${currentUser.bookmarks}")
        if (currentUser.bookmarks.isEmpty()) {
          _bookmarkedRecipes.value = emptyList()
          return@collectLatest
        }
        val bookmarkedRecipes = currentUser.bookmarks.map { recipeId ->
          async { Pair(currentUser, recipeUseCase.getRecipeByID(recipeId) ?: Recipe()) }
        }.awaitAll()

        _bookmarkedRecipes.value = bookmarkedRecipes
      }
    }
  }

  fun deleteRecipe(recipeId: String) {
    viewModelScope.launch {
      recipeUseCase.removeRecipe(recipeId)
    }
  }

}