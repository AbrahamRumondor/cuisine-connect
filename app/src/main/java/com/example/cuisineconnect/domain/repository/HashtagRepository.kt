package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback

interface HashtagRepository {
  fun updateHashtagWithScore(hashtagBody: String, newTimestamp: Long, increment: Int)
  fun getTrendingHashtags(callback: TrendingHashtagsCallback)
  fun addHashtag(hashtagBody: String, callback: (Boolean, Exception?) -> Unit)
}
