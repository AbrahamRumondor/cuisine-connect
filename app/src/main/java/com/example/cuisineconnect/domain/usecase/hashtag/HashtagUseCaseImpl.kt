package com.example.cuisineconnect.domain.usecase.hashtag

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.repository.HashtagRepository
import javax.inject.Inject

class HashtagUseCaseImpl @Inject constructor(
  private val hashtagRepository: HashtagRepository
) : HashtagUseCase {
  override fun updateHashtagWithScore(
    hashtagBody: String,
    itemId: String,
    newTimestamp: Long,
    increment: Int
  ) {
    hashtagRepository.updateHashtagWithScore(hashtagBody, itemId, newTimestamp, increment)
  }

  override fun getTrendingHashtags(callback: TrendingHashtagsCallback) {
    hashtagRepository.getTrendingHashtags(callback)
  }

  override fun addHashtag(
    hashtagBody: String,
    itemId: String,
    newTimestamp: Long,
    increment: Int,
    callback: (Boolean, Exception?) -> Unit
  ) {
    hashtagRepository.addHashtag(hashtagBody, itemId, newTimestamp, increment, callback)
  }

  override fun searchHashtags(query: String, callback: (List<Hashtag>, Exception?) -> Unit) {
    hashtagRepository.searchHashtags(query, callback)
  }

  override fun findSearchPromptHashtags(
    query: String,
    callback: (List<Hashtag>, Exception?) -> Unit
  ) {
    hashtagRepository.findSearchPromptHashtags(query, callback)
  }
}