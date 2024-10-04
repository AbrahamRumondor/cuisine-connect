package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Post
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class PostResponse(
  @get:PropertyName("post_id")
  @set:PropertyName("post_id")
  var id: String = "", // userId_postId

  @get:PropertyName("post_date")
  @set:PropertyName("post_date")
  var date: Date = Date(),

  @get:PropertyName("post_upvotes")
  @set:PropertyName("post_upvotes")
  var upvotes: Map<String, Boolean> = emptyMap(),

  @get:PropertyName("post_content")
  @set:PropertyName("post_content")
  var postContent: MutableList<MutableMap<String, String>> = mutableListOf(),

  @get:PropertyName("post_reply_count")
  @set:PropertyName("post_reply_count")
  var replyCount: Int = 0,

  @get:PropertyName("post_bookmark_count")
  @set:PropertyName("post_bookmark_count")
  var bookmarkCount: Int = 0
) {

  constructor() : this("")

  companion object {
    fun transform(postResponse: PostResponse): Post {
      return Post(
        id = postResponse.id,
        date = postResponse.date,
        upvotes = postResponse.upvotes,
        bookmarkCount = postResponse.bookmarkCount,
        replyCount = postResponse.replyCount,
        postContent = postResponse.postContent
      )
    }

    fun transform(post: Post): PostResponse {
      return PostResponse(
        id = post.id,
        date = post.date,
        upvotes = post.upvotes,
        bookmarkCount = post.bookmarkCount,
        replyCount = post.replyCount,
        postContent = post.postContent
      )
    }
  }
}