package com.example.cuisineconnect.app.screen.post.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.hashtag.HashtagUseCase
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.reply.ReplyUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase,
  private val hashtagUseCase: HashtagUseCase
) : ViewModel() {
  private val storageReference = FirebaseStorage.getInstance().reference

  var postContent: MutableList<MutableMap<String, String>> = mutableListOf()
  val imageList: MutableList<Uri> = mutableListOf()

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  private val _replyCount: MutableStateFlow<Int> = MutableStateFlow(0)
  val replyCount: StateFlow<Int> = _replyCount

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

  fun getPostHashtags(postContent: List<Map<String, String>>): List<String> {
    val hashtagRegex = Regex("#\\w+") // Matches hashtags starting with '#' and followed by word characters
    val hashtags = mutableListOf<String>()

    postContent.forEach { contentItem ->
      val type = contentItem["type"]
      val value = contentItem["value"]

      // Check if the type contains "text" and the value is not null
      if (type == "text" && value != null) {
        // Find all matches for the hashtag pattern
        hashtagRegex.findAll(value).forEach { match ->
          hashtags.add(match.value) // Add the matched hashtag to the list
        }
      }
    }
    return hashtags
  }

  fun updateTrendingCounter(hashtagsBody: List<String>, itemId: String) {
    viewModelScope.launch {
      val newTimestamp = Date().time
      val increment = 1
      hashtagsBody.forEach { hashtagBody ->
        try {
          hashtagUseCase.updateHashtagWithScore(hashtagBody, itemId, newTimestamp, increment)
        } catch (e: Exception) {
          Timber.tag("HASHTAG_UPDATE_ERROR")
            .e("Failed to update hashtag $hashtagBody: ${e.message}")
        }
      }
    }
  }
}