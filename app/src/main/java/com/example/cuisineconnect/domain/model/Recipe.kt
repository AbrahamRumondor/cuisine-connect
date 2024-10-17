package com.example.cuisineconnect.domain.model

import java.util.Date

data class Recipe(
  var id: String = "", // userId_recipeId
  var title: String = "",
  var description: String = "",
  var portion: Int = 0,
  var duration: Int = 0,
  var ingredients: List<String> = listOf(),
  var category: List<String> = listOf(),
  var date: Date = Date(),
  var upvotes: Map<String, Boolean> = emptyMap(),
  var image: String = "",
  var replyCount: Int = 0,
  var bookmarkCount: Int = 0,
  var referencedBy: Map<String, Boolean> = emptyMap(),
  var userId: String = "",
)