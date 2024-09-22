package com.example.cuisineconnect.domain.usecase.reply

import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.repository.ReplyRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ReplyUseCaseImpl @Inject constructor(
  private val replyRepository: ReplyRepository
) : ReplyUseCase {
  override suspend fun getRepliesByRecipe(recipeId: String): StateFlow<List<Reply>> {
    return replyRepository.getRepliesByRecipe(recipeId)
  }

  override suspend fun getReplyById(recipeId: String, replyId: String): Reply? {
    return replyRepository.getReplyById(recipeId, replyId)
  }

  override fun setReply(recipeId: String, replyId: String, replyResponse: ReplyResponse) {
    replyRepository.setReply(recipeId, replyId, replyResponse)
  }

  override fun getRecipeReplyDocID(recipeId: String): String {
    return replyRepository.getRecipeReplyDocID(recipeId)
  }

  override fun getChildReplyDocID(recipeId: String, rootReplyId: String): String {
    return replyRepository.getChildReplyDocID(recipeId, rootReplyId)
  }

  override suspend fun upvoteReply(recipeId: String, replyId: String) {
    replyRepository.upvoteReply(recipeId, replyId)
  }

  override suspend fun removeUpvote(recipeId: String, replyId: String) {
    replyRepository.removeUpvote(recipeId, replyId)
  }

}