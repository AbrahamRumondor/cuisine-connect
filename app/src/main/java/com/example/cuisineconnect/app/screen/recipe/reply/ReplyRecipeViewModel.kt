package com.example.cuisineconnect.app.screen.recipe.reply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.reply.ReplyUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReplyRecipeViewModel @Inject constructor(
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase,
  private val replyUseCase: ReplyUseCase
) : ViewModel() {

  private val _replies: MutableStateFlow<List<Pair<User?, Reply>>> =
    MutableStateFlow(mutableListOf(Pair(User(), Reply())))
  val replies: StateFlow<List<Pair<User?, Reply>>> = _replies

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

  fun getRepliesByRecipe(recipeId: String) {
    viewModelScope.launch {
      replyUseCase.getRepliesByRecipe(recipeId).collectLatest { replies ->
        // Fetch users in parallel using async
        val userReplies = replies.map { reply ->
          async {
            val user = userUseCase.getUserByUserId(reply.userId) // Suspend function
            Pair(user, reply)
          }
        }.awaitAll() // Wait for all async tasks to complete

        // Update the StateFlow with the new list of pairs
        _replies.value = userReplies
      }
    }
  }

  fun getReplyById(recipeId: String, rootReply: String, replyIds: List<String>) {
    viewModelScope.launch {
      val rootIndex = _replies.value.indexOfFirst { it.second.id == rootReply }

      if (rootIndex != -1) {
        replyIds.forEach { replyId ->
          replyUseCase.getReplyById(recipeId, replyId)?.let { reply ->
            val user = userUseCase.getUserByUserId(reply.userId)
            val newReply = reply.copy(isRoot = 1)
            val userReplyPair = Pair(user, newReply)

            // Create a mutable copy of the current list of Pair<User, Reply>
            val updatedReplies = _replies.value.toMutableList()

            // Insert the new reply right after the rootReply
            updatedReplies.add(rootIndex + 1, userReplyPair)

            // Update the StateFlow with the new list
            _replies.value = updatedReplies
          }
        }
      }
    }
  }

  private fun getRecipeReplyDocID(recipeId: String): String {
    return replyUseCase.getRecipeReplyDocID(recipeId)
  }

  fun createReplyResponse(
    body: String,
    repliesId: String,
    userId: String,
    recipeId: String
  ): ReplyResponse {
    val newReplyId = getRecipeReplyDocID(recipeId)

    viewModelScope.launch {
      _replies.value.find { it.second.id == repliesId }?.let { pair ->
        val originalReply = pair.second

        val updatedRepliesId = originalReply.repliesId.toMutableList().apply {
          add(newReplyId)
        }

        val updatedReply = originalReply.copy(repliesId = updatedRepliesId)

        val updatedReplies = _replies.value.toMutableList().apply {
          val index = indexOfFirst { it.second.id == repliesId }
          set(index, Pair(pair.first, updatedReply)) // Replace the old reply with the updated one
        }
        _replies.value = updatedReplies

        // Update the reply in Firestore
        setReply(newReplyId, ReplyResponse.transform(updatedReply)){

        }
      }
    }

    // Create the new ReplyResponse to be added
    return ReplyResponse(
      id = newReplyId,
      body = body,
      date = Date(),
      repliesId = mutableListOf(), // No replies yet, empty list
      upvotes = 0,
      userId = userId
    )
  }

  fun setReply(recipeId: String, replyResponse: ReplyResponse, result: (ReplyResponse) -> Unit) {
    replyUseCase.setReply(recipeId, replyResponse.id, replyResponse)
    result(replyResponse)
  }

  //  fun getRecipeReplyDocID(recipeId: String): String
//  fun getChildReplyDocID(rootReplyId: String): String {
//    return replyUseCase.getChildReplyDocID(rootReplyId)
//  }

  fun upvoteReply(recipeId: String, replyId: String) {
    viewModelScope.launch {
      replyUseCase.upvoteReply(recipeId, replyId)
    }
  }

  fun removeUpvote(recipeId: String, replyId: String) {
    viewModelScope.launch {
      replyUseCase.removeUpvote(recipeId, replyId)
    }
  }

}