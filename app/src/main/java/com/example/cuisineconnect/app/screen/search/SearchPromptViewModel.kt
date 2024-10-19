package com.example.cuisineconnect.app.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchPromptViewModel @Inject constructor(
  private val userUseCase: UserUseCase
) : ViewModel() {

  private val _users: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())
  val users: StateFlow<List<User>> = _users

  private val _prompt: MutableStateFlow<String> = MutableStateFlow("")
  val prompt: StateFlow<String> = _prompt

  // StateFlow to hold combined search results
  val searchResults: StateFlow<List<Any?>> = combine(prompt, users) { prompt, users ->
    val itemsToSubmit = mutableListOf<Any?>()

    if (prompt.isNotEmpty()) {
      itemsToSubmit.add(prompt)
      itemsToSubmit.add(0)
      itemsToSubmit.addAll(users.ifEmpty { listOf("No users found") })
    } else {
      itemsToSubmit.add("Type something to search...")
    }

    itemsToSubmit.toList()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Job for the debounce delay
  private var searchJob: Job? = null

  // Method to update the search prompt with debounce
  fun updatePrompt(newPrompt: String) {
    // Cancel previous job if it exists
    searchJob?.cancel()

    // Launch a new coroutine for the debounce logic
    searchJob = viewModelScope.launch {
      // Add a delay (e.g., 300 milliseconds)
      delay(300)

      // Update the prompt state
      _prompt.value = newPrompt

      // Fetch users only if the prompt is not empty
      if (newPrompt.isNotEmpty()) {
        fetchUsersStartingWith(newPrompt)
      } else {
        _users.value = emptyList() // Clear users if prompt is empty
      }
    }
  }

  private fun fetchUsersStartingWith(prefix: String) {
    viewModelScope.launch {
      val fetchedUsers = userUseCase.getUsersStartingWith(prefix)
      _users.value = fetchedUsers
    }
  }
}