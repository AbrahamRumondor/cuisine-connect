package com.example.cuisineconnect.app.screen.recipe.reply

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.callbacks.ReplyCountCallback
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
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
  private val userUseCase: UserUseCase,
  private val replyUseCase: ReplyUseCase
) : ViewModel() {

  private val _replies: MutableStateFlow<List<Triple<User?, Reply, User?>>?> =
    MutableStateFlow(null)
  val replies: StateFlow<List<Triple<User?, Reply, User?>>?> = _replies

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  val openedReply = mutableListOf<String>()

  init {
    getUser()
  }

  private fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }

  fun addOpenedReply(reply: Reply) {
    if (reply.parentId in openedReply) {
      openedReply.add(reply.id)
    }
  }

  fun getRepliesByRecipe(recipeId: String, openReply: String?) {
    if (openReply == null) openedReply.clear()
    else if (openReply.isNotEmpty()) openedReply.add(openReply)

    viewModelScope.launch {
      replyUseCase.getRepliesByRecipeOrPost(recipeId).collectLatest { replies ->

        // Fetch users in parallel using async
        val userReplies = replies.map { reply ->
          async {
            val user = userUseCase.getUserByUserId(reply.userId) // Suspend function
            val replyTarget = replyUseCase.getReplyById(recipeId, reply.parentId)
            val targetUser = replyTarget?.let { userUseCase.getUserByUserId(it.userId) }
            Triple(user, reply, targetUser)
          }
        }.awaitAll() // Wait for all async tasks to complete
        // Update the StateFlow with the new list of pairs
        _replies.value = sortUserReplies(recipeId, userReplies)
      }
    }
  }

  private fun sortUserReplies(
    recipeId: String,
    userReplies: List<Triple<User?, Reply, User?>>
  ): List<Triple<User?, Reply, User?>> {
    // Group replies by their parentId
    val replyMap = userReplies.groupBy { it.second.parentId }
    val result = mutableListOf<Triple<User?, Reply, User?>>()

    // Recursive function to add replies to the result
    val semiResult = mutableListOf<Triple<User?, Reply, User?>>()
    fun addRepliesToResult(rootReply: String, parentId: String?) {
      // Get replies that have this parentId
      replyMap[parentId]?.forEach { replyPair ->
        // Add the reply to the result if its root reply is opened
        if (rootReply in openedReply) {
          semiResult.add(Triple(replyPair.first, replyPair.second.copy(isRoot = 1), replyPair.third))
        }

        // Recursively add all children of this reply
        addRepliesToResult(rootReply, replyPair.second.id)
      }
    }

    // Start from the root replies
    replyMap[recipeId]?.forEach { rootReplyPair ->
      // Add the root reply to the result
      val rootReply = rootReplyPair.second.copy(isRoot = 0)
      result.add(Triple(rootReplyPair.first, rootReply, rootReplyPair.third))

      // Recursively add all replies under this root reply
      addRepliesToResult(rootReplyPair.second.id, rootReplyPair.second.id)
      result.addAll(semiResult.sortedBy { it.second.date })
      semiResult.clear()
    }

    // Sort the result by reply_date, descending
    return result
  }

//  fun getReplyById(recipeId: String, rootReply: String, replyIds: List<String>) {
//    viewModelScope.launch {
//      val rootIndex = _replies.value?.indexOfFirst { it.second.id == rootReply }
//
//      if (rootIndex != -1) {
//        replyIds.forEach { replyId ->
//          replyUseCase.getReplyById(recipeId, replyId)?.let { reply ->
//            val user = userUseCase.getUserByUserId(reply.userId)
//            val newReply = reply.copy(isRoot = 1)
//            val userReplyPair = Pair(user, newReply)
//
//            // Create a mutable copy of the current list of Pair<User, Reply>
//            val updatedReplies = _replies.value?.toMutableList()
//
//            // Insert the new reply right after the rootReply
//            if (rootIndex != null) {
//              updatedReplies?.add(rootIndex + 1, userReplyPair)
//            }
//
//            // Update the StateFlow with the new list
//            _replies.value = updatedReplies
//          }
//        }
//      }
//    }
//  }

  private fun getRecipeReplyDocID(itemId: String): String {
    return replyUseCase.getReplyDocID(itemId)
  }

  fun createReplyResponse(
    body: String,
    repliesId: String,
    userId: String,
    itemId: String
  ): ReplyResponse {
    val newReplyId = getRecipeReplyDocID(itemId)

    if (repliesId != itemId) {
      viewModelScope.launch {
        _replies.value?.find { it.second.id == repliesId }?.let { triple ->
          val originalReply = triple.second

          val updatedRepliesId = originalReply.repliesId.toMutableList().apply {
            add(newReplyId)
          }

          val updatedReply = originalReply.copy(repliesId = updatedRepliesId)

          // First, update Firestore and then update the local list
          setReply(itemId, ReplyResponse.transform(updatedReply), isNewReply = false) {
            Log.d("brobruh", "WORKING")

            // Now update the local list once the Firestore operation succeeds
            val updatedReplies = _replies.value!!.toMutableList().apply {
              val index = indexOfFirst { it.second.id == repliesId }
              set(
                index,
                Triple(triple.first, updatedReply, triple.third)
              ) // Replace the old reply with the updated one
            }
            _replies.value = updatedReplies
          }
        }
      }
    }

    // Create the new ReplyResponse to be added
    return ReplyResponse(
      id = newReplyId,
      body = body,
      date = Date(),
      repliesId = mutableListOf(), // No replies yet, empty list
      userId = userId,
      parentId = repliesId
    )
  }

  fun setReply(recipeId: String, replyResponse: ReplyResponse, isNewReply: Boolean, result: (ReplyResponse) -> Unit) {
    replyUseCase.setReply(recipeId, replyResponse.id, replyResponse, isNewReply)
    result(replyResponse)
  }

  //  fun getRecipeReplyDocID(recipeId: String): String
//  fun getChildReplyDocID(rootReplyId: String): String {
//    return replyUseCase.getChildReplyDocID(rootReplyId)
//  }

  fun upvoteReply(recipeId: String, replyId: String, userId: String, result: (Reply) -> Unit) {
    viewModelScope.launch {
      replyUseCase.upvoteReply(recipeId, replyId, userId, result)
    }
  }

  fun downVoteReply(recipeId: String, replyId: String, userId: String, result: (Reply) -> Unit) {
    viewModelScope.launch {
      replyUseCase.removeUpvote(recipeId, replyId, userId, result)
    }
  }

  fun fetchTotalReplyCount(itemId: String, replyId: String, callback: ReplyCountCallback) {
    viewModelScope.launch {
      try {
        val count = replyUseCase.getTotalReplyCount(itemId, replyId)
        callback.onReplyCountRetrieved(count) // Use the callback to pass the count
      } catch (e: Exception) {
        callback.onError(e) // Call error if any
      }
    }
  }

}