package com.example.cuisineconnect.app.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LOG_TAG
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.pagingSource.FeedItem
import com.example.cuisineconnect.data.pagingSource.SearchResultPagingSource
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.firestore.CollectionReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SearchResultViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase,
  @Named("postsRef") private val postsRef: CollectionReference,
  @Named("recipesRef") private val recipesRef: CollectionReference,
  @Named("usersRef") private val usersRef: CollectionReference,
  @Named("hashtagsRef") private val hashtagsRef: CollectionReference
) : ViewModel() {

  private val _searchQuery: MutableStateFlow<Pair<List<String>?, String?>> = MutableStateFlow(
    Pair(
      mutableListOf(), ""
    )
  )
  val searchQuery: StateFlow<Pair<List<String>?, String?>> = _searchQuery.asStateFlow()

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user.asStateFlow()

  init {
    getUser()
  }

  // Use flatMapLatest to create a new Pager whenever the searchQuery changes
  @OptIn(ExperimentalCoroutinesApi::class)
  val postsNRecipesList: Flow<PagingData<FeedItem>> = _searchQuery
    .flatMapLatest { query ->
      Pager(PagingConfig(pageSize = 10)) {
        SearchResultPagingSource(postsRef, recipesRef, usersRef, hashtagsRef, query)
      }.flow.cachedIn(viewModelScope)
    }

  fun updateSearchQuery(query: Pair<List<String>?, String?>) {
    _searchQuery.value = query
  }

  fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }
}