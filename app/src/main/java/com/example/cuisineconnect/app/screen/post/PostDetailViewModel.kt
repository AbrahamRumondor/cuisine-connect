package com.example.cuisineconnect.app.screen.post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class PostDetailViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {
  private val storageReference = FirebaseStorage.getInstance().reference

  var postContent: MutableList<MutableMap<String, String>> = mutableListOf()
  val imageList: MutableList<Uri> = mutableListOf()

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  private fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }

  fun getRecipeById(recipeId: String, result: (Pair<User, Recipe>?) -> Unit) {
    viewModelScope.launch {
      // Fetch the recipe
      val recipe = withContext(Dispatchers.IO) {
        recipeUseCase.getRecipeByID(recipeId)
      }

      // If recipe is not null, fetch the user
      val user = recipe?.let {
        val userId = it.id.substringAfter("_").substringBefore("_")
        withContext(Dispatchers.IO) {
          userUseCase.getUserByUserId(userId)
        }
      }

      // Set the value of _recipes if both recipe and user are available
      recipe?.let { r ->
        user?.let { u ->
          result(Pair(u, r))
        }
      } ?: result(null)
    }
  }

  fun getPostById(postId: String, result: (Pair<User, Post>?) -> Unit) {
    viewModelScope.launch {
      // Fetch the post first
      val post = postUseCase.getPostByID(postId)

      // Only if post is not null, proceed to fetch the user
      val user = post?.let {
        async { userUseCase.getUserByUserId(it.userId) }.await()
      }

      // Return the result as a Pair<User, Post> if both post and user are non-null
      result(if (post != null && user != null) Pair(user, post) else null)
    }
  }

  fun upvotePost(postId: String, userId: String, result: (Post) -> Unit) {
    viewModelScope.launch {
      postUseCase.upvotePost(postId, userId, result)
    }
  }

  fun downVotePost(recipeId: String, userId: String, result: (Post) -> Unit) {
    viewModelScope.launch {
      postUseCase.downVotePost(recipeId, userId, result)
    }
  }

  fun addToBookmark(
    postId: String,
    userId: String,
    result: (Post) -> Unit
  ) {
    viewModelScope.launch {
      postUseCase.addToBookmark(postId, userId, result)
    }
  }

  fun removeFromBookmark(
    postId: String,
    userId: String,
    result: (Post) -> Unit
  ) {
    viewModelScope.launch {
      postUseCase.removeFromBookmark(postId, userId, result)
    }
  }
}