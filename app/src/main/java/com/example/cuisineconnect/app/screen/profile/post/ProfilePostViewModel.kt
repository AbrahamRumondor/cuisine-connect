package com.example.cuisineconnect.app.screen.profile.post

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
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProfilePostViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Any?>>?> =
    MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Any?>>?> = _list

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  private fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
      getPostNRecipeOfUser()
    }
  }

  fun getPostNRecipeOfUser() {
    viewModelScope.launch {
      user.collectLatest { user ->
        // Fetch posts and recipes asynchronously
        val recipesDeferred = user.recipes.map { recipeId ->
          async {
            _user.value = userUseCase.getCurrentUser().value
            currentUser = _user.value
            val recipe = recipeUseCase.getRecipeByID(recipeId)
            Pair(user, recipe) // Pair with user
          }
        }

        val postsDeferred = user.posts.map { postId ->
          async {
            val post = postUseCase.getPostByID(postId)
            Pair(user, post) // Pair with user
          }
        }

        // Await all posts and recipes to complete
        val recipes = recipesDeferred.awaitAll()
        val posts = postsDeferred.awaitAll()

        // Combine posts and recipes into a single list
        val combinedList = recipes + posts

        // Sort the combined list by date in descending order
        val sortedList = combinedList.sortedByDescending { pair ->
          when (val item = pair.second) {
            is Recipe -> item.date // Assuming Recipe has a 'date' property
            is Post -> item.date // Assuming Post has a 'date' property
            else -> Date(0) // Default to the earliest date if the type is unknown
          }
        }

        // Update the StateFlow with the sorted list
        _list.value = sortedList
      }
    }
  }

}