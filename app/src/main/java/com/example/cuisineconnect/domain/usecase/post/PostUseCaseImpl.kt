package com.example.cuisineconnect.domain.usecase.post

import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.PostRepository
import com.example.cuisineconnect.domain.repository.RecipeRepository
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PostUseCaseImpl @Inject constructor(
  private val postRepository: PostRepository
) : PostUseCase {
  override suspend fun getPosts(): StateFlow<List<Post>> {
    return postRepository.getPosts()
  }

  override suspend fun getMyPost(userId: String): StateFlow<List<Post>> {
    return postRepository.getMyPost(userId)
  }

  override fun getPostDocID(userId: String): String {
    return postRepository.getPostDocID(userId)
  }

  override suspend fun setPost(postId: String, postResponse: PostResponse, result: () -> Unit) {
    postRepository.setPost(postId, postResponse, result)
  }

  override suspend fun getPostByID(postId: String): Post? {
    return postRepository.getPostByID(postId)
  }

  override suspend fun upvotePost(postId: String, userId: String, result: (Post) -> Unit) {
    postRepository.upvotePost(postId, userId, result)
  }

  override suspend fun downVotePost(postId: String, userId: String, result: (Post) -> Unit) {
    postRepository.downVotePost(postId, userId, result)
  }

  override suspend fun removePost(postId: String) {
    postRepository.removePost(postId)
  }

  override suspend fun getPostsForHome(userId: String): StateFlow<List<Pair<User, Post>>> {
    return postRepository.getPostsForHome(userId)
  }
}