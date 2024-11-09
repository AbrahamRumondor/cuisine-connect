package com.example.cuisineconnect.domain.model

import java.util.Date

data class Recipe(
  var id: String = "", // userId_recipeId
  var title: String = "",
  var description: String = "",
  var portion: String = "",
  var duration: String = "",
  var ingredients: List<String> = listOf(),
  var hashtags: List<String> = emptyList(),
  var date: Date = Date(),
  var upvotes: Map<String, Boolean> = emptyMap(),
  var image: String = "",
  var replyCount: Int = 0,
  var bookmarks: Map<String, Boolean> = emptyMap(),
  var referencedBy: Map<String, Boolean> = emptyMap(),
  var userId: String = "",
  var recipeTitleSplit: List<String> = listOf()
)