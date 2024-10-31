package com.example.cuisineconnect.app.screen.profile.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
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
class ProfilePostViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {

  private val _list: MutableStateFlow<List<Pair<User, Post?>>?> = MutableStateFlow(null)
  val list: StateFlow<List<Pair<User, Post?>>?> = _list

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
      getPostsOfUser()
    }
  }

  fun getPostsOfUser() {
    viewModelScope.launch {
      user.collectLatest { user ->
        // Fetch posts asynchronously
        val postsDeferred = user.posts.map { postId ->
          async {
            val post = postUseCase.getPostByID(postId)
            Pair(user, post) // Pair with user
          }
        }

        // Await all posts to complete
        val posts = postsDeferred.awaitAll()

        // Sort the posts by date in descending order
        val sortedPosts = posts
          .filter { it.second is Post }
          .sortedByDescending { (it.second as Post).date.time } // Assuming Post has a 'date' property of type Date

        // Update the StateFlow with the sorted posts
        _list.value = sortedPosts
      }
    }
  }

  fun deletePost(postId: String) {
    viewModelScope.launch {
      postUseCase.removePost(postId)
    }
  }
}