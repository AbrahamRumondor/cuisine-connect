package com.example.cuisineconnect.app.screen.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.domain.model.User
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
class FollowListViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
) : ViewModel() {

  private val _otherUser: MutableStateFlow<User> = MutableStateFlow(User())
  val otherUser: StateFlow<User> = _otherUser

  // StateFlows for followers and following lists
  private val _myFollowers: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val myFollowers: StateFlow<List<User>> = _myFollowers

  private val _myFollowing: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val myFollowing: StateFlow<List<User>> = _myFollowing

  private val _otherUserFollowers: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val otherUserFollowers: StateFlow<List<User>> = _otherUserFollowers

  private val _otherUserFollowing: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val otherUserFollowing: StateFlow<List<User>> = _otherUserFollowing

  fun getOtherUser(userId: String) {
    viewModelScope.launch {
      val user = userUseCase.getUserByUserId(userId)
      user?.let {
        _otherUser.value = it
      }
    }
  }

  fun getMyFollowers() {
    currentUser?.let { currentUser ->
      viewModelScope.launch {
        val users = mutableListOf<User>()
        val deferredFollowers = currentUser.follower.map { followerId ->
          async {
            userUseCase.getUserByUserId(followerId)
          }
        }

        val results = deferredFollowers.awaitAll()
        results.forEach { user ->
          user?.let { users.add(it) }
        }
        _myFollowers.value = users // Update StateFlow
      }
    }
  }

  fun getMyFollowing() {
    currentUser?.let { currentUser ->
      viewModelScope.launch {
        val users = mutableListOf<User>()
        val deferredFollowing = currentUser.following.map { followingId ->
          async {
            userUseCase.getUserByUserId(followingId)
          }
        }

        val results = deferredFollowing.awaitAll()
        results.forEach { user ->
          user?.let { users.add(it) }
        }
        _myFollowing.value = users // Update StateFlow
      }
    }
  }

  fun otherUserFollower() {
    viewModelScope.launch {
      otherUser.collectLatest { currentUser ->
        val users = mutableListOf<User>()
        val deferredFollowers = currentUser.follower.map { followerId ->
          async {
            userUseCase.getUserByUserId(followerId)
          }
        }

        val results = deferredFollowers.awaitAll()
        results.forEach { user ->
          user?.let { users.add(it) }
        }
        _otherUserFollowers.value = users // Update StateFlow
      }
    }
  }

  fun otherUserFollowing() {
    viewModelScope.launch {
      otherUser.collectLatest { currentUser ->
        val users = mutableListOf<User>()
        val deferredFollowing = currentUser.following.map { followingId ->
          async {
            userUseCase.getUserByUserId(followingId)
          }
        }

        val results = deferredFollowing.awaitAll()
        results.forEach { user ->
          user?.let { users.add(it) }
        }
        _otherUserFollowing.value = users // Update StateFlow
      }
    }
  }
}