package com.example.cuisineconnect.domain.usecase.hashtag

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback

interface HashtagUseCase {
  fun updateHashtagWithScore(hashtagBody: String, newTimestamp: Long, increment: Int)
  fun getTrendingHashtags(callback: TrendingHashtagsCallback)
  fun addHashtag(hashtagBody: String, callback: (Boolean, Exception?) -> Unit)
}
