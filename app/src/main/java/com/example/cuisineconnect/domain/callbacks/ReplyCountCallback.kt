package com.example.cuisineconnect.domain.callbacks

interface ReplyCountCallback {
    fun onReplyCountRetrieved(count: Int)
    fun onError(e: Exception) // Optional: To handle any errors
}