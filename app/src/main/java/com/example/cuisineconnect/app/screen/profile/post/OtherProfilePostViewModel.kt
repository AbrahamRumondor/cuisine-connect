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
import javax.inject.Inject

@HiltViewModel
class OtherProfilePostViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Any?>>?> =
    MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Any?>>?> = _list

  private val _otherUser: MutableStateFlow<User> = MutableStateFlow(User())
  val otherUser: StateFlow<User> = _otherUser

  fun getUser(userId: String) {
    viewModelScope.launch {
      val user = userUseCase.getUserByUserId(userId)
      user?.let {
        _otherUser.value = it
        getPostNRecipeOfUser()
      }
    }
  }

  fun getPostNRecipeOfUser() {
    viewModelScope.launch {
      _otherUser.collectLatest { user ->
        // Fetch posts and recipes asynchronously
        val recipesDeferred = user.recipes.map { recipeId ->
          async {
            val recipe = recipeUseCase.getRecipeByID(recipeId)
            Pair(user, recipe)
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
        val sortedList = combinedList
          .filter { pair ->
            pair.second is Recipe || pair.second is Post
          }
          .sortedByDescending { pair ->
            when (val item = pair.second) {
              is Recipe -> item.date.time // Assuming Recipe has a 'date' property of type Date
              is Post -> item.date.time // Assuming Post has a 'date' property of type Date
              else -> 0L // This won't be used since non-Recipe/Post are filtered
            }
          }

        // Update the StateFlow with the sorted list
        _list.value = sortedList
      }
    }
  }
}