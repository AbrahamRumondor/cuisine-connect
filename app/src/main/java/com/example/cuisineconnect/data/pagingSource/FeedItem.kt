package com.example.cuisineconnect.data.pagingSource

import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User

sealed class FeedItem {
    data class PostItem(val user: User?, val post: Post) : FeedItem()
    data class RecipeItem(val user: User?, val recipe: Recipe) : FeedItem()
}