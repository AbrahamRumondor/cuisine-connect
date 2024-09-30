package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import kotlinx.coroutines.flow.StateFlow

interface ReplyRepository {
  suspend fun getRepliesByRecipe(recipeId: String): StateFlow<List<Reply>>
  suspend fun getReplyById(recipeId: String, replyId: String): Reply?
  fun setReply(recipeId: String, replyId: String, replyResponse: ReplyResponse, isNewReply: Boolean)
  fun getRecipeReplyDocID(recipeId: String): String
  fun getChildReplyDocID(recipeId: String, rootReplyId: String): String
  suspend fun upvoteReply(recipeId: String, repliedId: String, userId: String, result: (Reply) -> Unit)
  suspend fun removeUpvote(recipeId: String, repliedId: String, userId: String, result: (Reply) -> Unit)
}
