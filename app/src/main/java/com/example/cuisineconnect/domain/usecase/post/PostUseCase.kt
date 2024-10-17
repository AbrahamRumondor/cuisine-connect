package com.example.cuisineconnect.domain.usecase.post

import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PostUseCase {
  suspend fun getPosts(): StateFlow<List<Post>> // Use Flow instead of StateFlow for one-time fetch
  suspend fun getMyPost(userId: String): StateFlow<List<Post>>
  fun getPostDocID(userId: String): String
  suspend fun setPost(postId: String, postResponse: PostResponse, result: () -> Unit) // Mark as suspend for async operations
  suspend fun getPostByID(postId: String): Post?
  suspend fun upvotePost(postId: String, userId: String, result: (Post) -> Unit) // Renamed to upvotePost for consistency
  suspend fun downVotePost(postId: String, userId: String, result: (Post) -> Unit)
  suspend fun removePost(postId: String)
  suspend fun getPostsForHome(userId: String): StateFlow<List<Pair<User, Post>>>
}