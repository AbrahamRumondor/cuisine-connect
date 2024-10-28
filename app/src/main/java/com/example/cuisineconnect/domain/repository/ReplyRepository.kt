package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import kotlinx.coroutines.flow.StateFlow

interface ReplyRepository {
  suspend fun getRepliesByRecipeOrPost(id: String): StateFlow<List<Reply>>
  suspend fun getReplyById(id: String, replyId: String): Reply?
  fun setReply(id: String, replyId: String, replyResponse: ReplyResponse, isNewReply: Boolean)
  fun getReplyDocID(id: String): String
  fun getChildReplyDocID(id: String, rootReplyId: String): String
  suspend fun upvoteReply(id: String, repliedId: String, userId: String, result: (Reply) -> Unit)
  suspend fun removeUpvote(id: String, repliedId: String, userId: String, result: (Reply) -> Unit)
  suspend fun getTotalReplyCount(id: String, replyId: String): Int
}
