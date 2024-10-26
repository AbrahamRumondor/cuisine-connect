package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.PostRepository
import com.example.cuisineconnect.domain.repository.UserRepository
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class PostRepositoryImpl @Inject constructor(
  @Named("postsRef") private val postsRef: CollectionReference, // renamed from recipesRef to postsRef
  private val userRepository: UserRepository
) : PostRepository {

  private val _posts = MutableStateFlow<List<Post>>(emptyList()) // renamed _recipes to _posts
  private val posts: StateFlow<List<Post>> = _posts

  override suspend fun getPosts(): StateFlow<List<Post>> {
    try {
      val snapshot = postsRef.get().await() // renamed recipesRef to postsRef
      val postList = snapshot.toObjects(PostResponse::class.java)
      _posts.value =
        postList.map { PostResponse.transform(it) } // transforming PostResponse to Post
    } catch (e: Exception) {
      _posts.value = emptyList()
    }
    return posts
  }

  override suspend fun getMyPost(userId: String): StateFlow<List<Post>> {
    try {
      val snapshot = postsRef.get().await() // renamed recipesRef to postsRef
      val postList =
        snapshot.toObjects(PostResponse::class.java).filter { it.id.contains(userId) }
      _posts.value = postList.map { PostResponse.transform(it) }
    } catch (e: Exception) {
      _posts.value = emptyList()
    }
    return posts
  }

  override fun getPostDocID(userId: String): String {
    return "${userId}_${postsRef.document().id}_p" // renamed recipesRef to postsRef
  }

  override suspend fun setPost(
    postId: String,
    postResponse: PostResponse,
    result: () -> Unit
  ) { // updated to setPost
    postsRef.document(postId).set(postResponse).addOnSuccessListener {
      Timber.tag("TEST").d("SUCCESS ON post INSERTION") // changed "recipe" to "post"
      result()
    }
      .addOnFailureListener { Timber.tag("TEST").d("ERROR ON post INSERTION") }
  }

  override suspend fun getPostByID(postId: String): Post? { // updated to getPostByID
    return try {
      val snapshot = postsRef.document(postId).get().await()
      val post =
        snapshot.toObject(PostResponse::class.java)?.let { PostResponse.transform(it) }

      post
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun upvotePost(
    postId: String,
    userId: String,
    result: (Post) -> Unit
  ) { // renamed to upvotePost
    try {
      val postDoc = postsRef.document(postId) // renamed recipesRef to postsRef
      val snapshot = postDoc.get().await()
      val postResponse = snapshot.toObject(PostResponse::class.java)

      postResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()
        upvotes[userId] = true // Mark the user's upvote as true

        response.upvotes = upvotes
        postDoc.set(response).await()
        Timber.tag("Upvote").d("User $userId upvoted post $postId successfully")
        result(PostResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("Upvote").e(e, "Error upvoting post $postId by user $userId")
    }
  }

  override suspend fun downVotePost(
    postId: String,
    userId: String,
    result: (Post) -> Unit
  ) { // renamed to removeUpvote
    try {
      val postDoc = postsRef.document(postId) // renamed recipesRef to postsRef
      val snapshot = postDoc.get().await()
      val postResponse = snapshot.toObject(PostResponse::class.java)

      postResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()

        response.upvotes = upvotes.filterNot { it.key == userId }
        postDoc.set(response).await()
        Timber.tag("RemoveUpvote").d("User $userId removed upvote from post $postId successfully")
        result(PostResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("RemoveUpvote").e(e, "Error removing upvote from post $postId by user $userId")
    }
  }

  override suspend fun removePost(postId: String) {
    try {
      // Get a reference to the post document
      val postDoc = postsRef.document(postId)

      // Extract userId from postId (assuming postId is formatted as userId_postId)
      val userId = postId.substringBefore("_")

      // Get the current user
      val user = userRepository.getUserByUserId(userId)

      // Update user's posts by removing the postId
      user?.let {
        val updatedPosts =
          it.posts.filterNot { id -> id == postId } // Remove the postId from user's posts
        userRepository.storeUser(userId, user.copy(posts = updatedPosts)) // Store the updated user
      }

      // Delete the post document
      postDoc.delete().await()

      // Log success
      Timber.tag("RemovePost").d("Post $postId deleted successfully")
    } catch (e: Exception) {
      // Log error if deletion fails
      Timber.tag("RemovePost").e(e, "Error deleting post $postId")
    }
  }

  override suspend fun getPostsForHome(userId: String): StateFlow<List<Pair<User, Post>>> {
    val _postsFlow = MutableStateFlow<List<Pair<User, Post>>>(emptyList())

    // Fetch the user by userId
    val user = userRepository.getUserByUserId(userId) ?: return _postsFlow

    // Get the list of post IDs with associated user IDs
    val postsList = user.posts.map { postIdWithUser ->
      val authorId = postIdWithUser.substringBefore("_")

      // Perform the necessary asynchronous tasks concurrently
      coroutineScope {
        val authorDeferred = async { userRepository.getUserByUserId(authorId) }
        val postDeferred = async { getPostByID(postIdWithUser) }

        val author = authorDeferred.await()
        val post = postDeferred.await()

        // Return a Pair if both the author and post are non-null
        if (author != null && post != null) {
          Pair(author, post)
        } else {
          null
        }
      }
    }.filterNotNull() // Remove any null results

    // Update the state flow with the fetched posts
    _postsFlow.value = postsList
    return _postsFlow
  }

}