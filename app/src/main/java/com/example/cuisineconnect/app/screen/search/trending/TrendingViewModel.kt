package com.example.cuisineconnect.app.screen.search.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.hashtag.HashtagUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val hashtagUseCase: HashtagUseCase
) : ViewModel() {

  private val _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
  val currentUser: StateFlow<User?> = _currentUser

  init {
    fetchCurrentUser()
  }

  fun fetchCurrentUser() {
    viewModelScope.launch {
      try {
        userUseCase.getCurrentUser().collectLatest {
          _currentUser.value = it
        }
      } catch (e: Exception) {
        Timber.tag("USER_PROBLEM").e("Error fetching user: %s", e.message)
      }
    }
  }

  fun fetchTrendingHashtags(callback: (List<Hashtag>, Exception?) -> Unit) {
    val limit = 30
    hashtagUseCase.getSortedTrendingHashtags(limit, callback)
  }
}