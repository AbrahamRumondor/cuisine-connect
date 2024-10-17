package com.example.cuisineconnect.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.pagingSource.FeedItem
import com.example.cuisineconnect.data.pagingSource.HomePagingSource
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.firestore.CollectionReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainActivityViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase,
  @Named("postsRef") private val postsRef: CollectionReference, // renamed from recipesRef to postsRef
  @Named("recipesRef") private val recipesRef: CollectionReference,
  @Named("usersRef") private val usersRef: CollectionReference
) : ViewModel() {

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val postsNRecipesList: Flow<PagingData<FeedItem>> = user.flatMapLatest { currentUser ->
    // Create a new Pager every time the user changes
    Pager(PagingConfig(pageSize = 10)) {
      HomePagingSource(currentUser, postsRef, recipesRef, usersRef)
    }.flow.cachedIn(viewModelScope)
  }

  fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }

  fun deletePost(postId: String) {
    viewModelScope.launch {
      postUseCase.removePost(postId)
    }
  }

  fun deleteRecipe(recipeId: String) {
    viewModelScope.launch {
      recipeUseCase.removeRecipe(recipeId)
    }
  }
}