package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback
import com.example.cuisineconnect.domain.model.Hashtag

interface HashtagRepository {
  fun updateHashtagWithScore(
    hashtagBody: String,
    itemId: String,
    newTimestamp: Long,
    increment: Int
  )

  fun getTrendingHashtags(callback: TrendingHashtagsCallback)
  fun addHashtag(
    hashtagBody: String, itemId: String,
    newTimestamp: Long,
    increment: Int, callback: (Boolean, Exception?) -> Unit
  )

  fun searchHashtags(query: String, callback: (List<Hashtag>, Exception?) -> Unit)
  fun findSearchPromptHashtags(query: String, callback: (List<Hashtag>, Exception?) -> Unit)
  fun getSortedTrendingHashtags(limit: Int, callback: (List<Hashtag>, Exception?) -> Unit)
  suspend fun removeItemIdFromHashtag(hashtagBody: String, itemId: String)
}
