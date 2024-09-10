package com.example.cuisineconnect.app.screen.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
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

  private val _recipes: MutableStateFlow<List<Recipe>?> = MutableStateFlow(null)
  val recipes: StateFlow<List<Recipe>?> = _recipes

  init {
    getCollection()
  }

  private fun getCollection() {
    viewModelScope.launch {
      userUseCase.getCurrentUser().collectLatest { user ->
        // Use coroutineScope to launch parallel tasks
        coroutineScope {
          user.recipes.map { recipeId ->
            Log.d("brobruh", recipeId)
            async {
              recipeUseCase.getRecipeByID(recipeId) as Recipe
            }
          }.awaitAll().let { recipes ->
            Log.d("brobruh", recipes.toString())
            _recipes.value = recipes
          }
        }
      }
    }
  }

}