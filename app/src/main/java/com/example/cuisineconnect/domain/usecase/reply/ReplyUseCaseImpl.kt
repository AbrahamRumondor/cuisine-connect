package com.example.cuisineconnect.domain.usecase.reply

import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.repository.ReplyRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ReplyUseCaseImpl @Inject constructor(
  private val replyRepository: ReplyRepository
) : ReplyUseCase {
  override suspend fun getRepliesByRecipeOrPost(id: String): StateFlow<List<Reply>> {
    return replyRepository.getRepliesByRecipeOrPost(id)
  }

  override suspend fun getReplyById(id: String, replyId: String): Reply? {
    return replyRepository.getReplyById(id, replyId)
  }

  override fun setReply(
    id: String,
    replyId: String,
    replyResponse: ReplyResponse,
    isNewReply: Boolean
  ) {
    replyRepository.setReply(id, replyId, replyResponse, isNewReply)
  }

  override fun getReplyDocID(id: String): String {
    return replyRepository.getReplyDocID(id)
  }

  override fun getChildReplyDocID(id: String, rootReplyId: String): String {
    return replyRepository.getChildReplyDocID(id, rootReplyId)
  }

  override suspend fun upvoteReply(
    id: String,
    repliedId: String,
    userId: String,
    result: (Reply) -> Unit
  ) {
    replyRepository.upvoteReply(id, repliedId, userId, result)
  }

  override suspend fun removeUpvote(
    id: String,
    repliedId: String,
    userId: String,
    result: (Reply) -> Unit
  ) {
    replyRepository.removeUpvote(id, repliedId, userId, result)
  }

  override suspend fun getTotalReplyCount(itemId: String, replyId: String): Int {
    return replyRepository.getTotalReplyCount(itemId, replyId)
  }
}