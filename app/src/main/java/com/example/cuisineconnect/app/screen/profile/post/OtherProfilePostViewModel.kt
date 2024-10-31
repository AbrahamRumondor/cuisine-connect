package com.example.cuisineconnect.app.screen.profile.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
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
  private val userUseCase: UserUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Post>>?> = MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Post>>?> = _list

  private val _otherUser: MutableStateFlow<User> = MutableStateFlow(User())
  val otherUser: StateFlow<User> = _otherUser

  fun getUser(userId: String) {
    viewModelScope.launch {
      val user = userUseCase.getUserByUserId(userId)
      user?.let {
        _otherUser.value = it
        getPostsOfUser()  // Fetch only posts
      }
    }
  }

  fun getPostsOfUser() {
    viewModelScope.launch {
      _otherUser.collectLatest { user ->
        val postsDeferred = user.posts.map { postId ->
          async {
            postUseCase.getPostByID(postId)?.let { post ->
              Pair(user, post) // Pair with user
            }
          }
        }

        // Await and filter out null pairs
        val posts = postsDeferred.awaitAll().filterNotNull()

        // Sort posts by date in descending order
        val sortedPosts = posts.sortedByDescending { pair ->
          pair.second.date.time
        }

        // Update the StateFlow with the sorted list of posts
        _list.value = sortedPosts
      }
    }
  }
}