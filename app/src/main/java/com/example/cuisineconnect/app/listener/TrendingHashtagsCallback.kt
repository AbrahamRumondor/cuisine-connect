package com.example.cuisineconnect.app.listener

import com.example.cuisineconnect.domain.model.Hashtag

interface TrendingHashtagsCallback {
    fun onTrendingHashtagsReceived(hashtags: List<Hashtag>)
    fun onError(e: Exception)
}