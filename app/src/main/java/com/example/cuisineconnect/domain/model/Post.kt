package com.example.cuisineconnect.domain.model

import java.util.Date

data class Post(
  var id: String = "", // userId_recipeId
  var date: Date = Date(),
  var upvotes: Map<String, Boolean> = emptyMap(),
  val postContent: MutableList<MutableMap<String, String>> = mutableListOf(),
  var replyCount: Int = 0,
  var bookmarks: Map<String, Boolean> = emptyMap(),
  var referencedBy: Map<String, Boolean> = emptyMap(),
  var userId: String = ""
)