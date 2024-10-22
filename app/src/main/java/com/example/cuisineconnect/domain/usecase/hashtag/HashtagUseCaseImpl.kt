package com.example.cuisineconnect.domain.usecase.hashtag

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback
import com.example.cuisineconnect.domain.repository.HashtagRepository
import javax.inject.Inject

class HashtagUseCaseImpl @Inject constructor(
  private val hashtagRepository: HashtagRepository
) : HashtagUseCase {
  override fun updateHashtagWithScore(hashtagBody: String, newTimestamp: Long, increment: Int) {
    hashtagRepository.updateHashtagWithScore(hashtagBody, newTimestamp, increment)
  }

  override fun getTrendingHashtags(callback: TrendingHashtagsCallback) {
    hashtagRepository.getTrendingHashtags(callback)
  }

  override fun addHashtag(hashtagBody: String, callback: (Boolean, Exception?) -> Unit) {
    hashtagRepository.addHashtag(hashtagBody, callback)
  }
}