package com.example.cuisineconnect.app.screen.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.Sort
import com.example.cuisineconnect.app.util.Sort.Latest
import com.example.cuisineconnect.app.util.Sort.LeastLiked
import com.example.cuisineconnect.app.util.Sort.MostLiked
import com.example.cuisineconnect.app.util.Sort.Oldest
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

  private val _myRecipes: MutableStateFlow<List<Pair<User?, Recipe>>> =
    MutableStateFlow(emptyList())
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

  fun getMyRecipes(sortBy: Sort = Latest) {
    getRecipes() // Fetch all recipes without sorting

    viewModelScope.launch {
      recipes.collectLatest { allRecipes ->
        userUseCase.getCurrentUser().collectLatest { currentUser ->
          allRecipes?.let { recipes ->
            // Filter recipes to get only the user's recipes
            val userRecipes = recipes.filter { it.first?.id == currentUser.id }

            // Sort the filtered recipes based on the sortBy parameter
            _myRecipes.value = when (sortBy) {
              Latest -> userRecipes.sortedByDescending { it.second.date.time } // Sort by date descending
              MostLiked -> userRecipes.sortedByDescending { it.second.upvotes.size } // Sort by likes descending
              LeastLiked -> userRecipes.sortedBy { it.second.upvotes.size }
              Oldest -> userRecipes.sortedBy { it.second.date.time }
            }.ifEmpty { emptyList() }
          }

          // Log the updated _myRecipes value
          Log.d("brobruh", "tes: ${_myRecipes.value.toString()}")
        }
      }
    }
  }

  fun getBookmarkedRecipes(sortBy: Sort = Latest) {
    viewModelScope.launch {
      userUseCase.getCurrentUser().collectLatest { currentUser ->
        Log.d("collectionViewModel", "njir: ${currentUser.bookmarks}")
        if (currentUser.bookmarks.isEmpty()) {
          _bookmarkedRecipes.value = emptyList()
          return@collectLatest
        }
        val bookmarkedRecipes = currentUser.bookmarks.map { recipeId ->
          val userId = recipeId.split("_")[1]
          async {
            Pair(
              userUseCase.getUserByUserId(userId),
              recipeUseCase.getRecipeByID(recipeId) ?: Recipe()
            )
          }
        }.awaitAll()

        _bookmarkedRecipes.value = when (sortBy) {
          Latest -> bookmarkedRecipes.sortedByDescending { it.second.date.time } // Sort by date descending
          MostLiked -> bookmarkedRecipes.sortedByDescending { it.second.upvotes.size } // Sort by likes descending
          LeastLiked -> bookmarkedRecipes.sortedBy { it.second.upvotes.size }
          Oldest -> bookmarkedRecipes.sortedBy { it.second.date.time }
        }.ifEmpty { emptyList() }
      }
    }
  }

  fun deleteRecipe(recipeId: String) {
    viewModelScope.launch {
      recipeUseCase.removeRecipe(recipeId)
    }
  }

}