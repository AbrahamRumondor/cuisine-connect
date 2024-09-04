package com.example.cuisineconnect.domain.model

import java.util.Date

data class Recipe(
  var id: String = "", // userId_recipeId
  var title: String = "",
  var ingredients: List<String> = listOf(),
  var date: Date = Date(),
  var upvote: Int = 0,
  var category: List<String> = listOf(),
  var image: String = "",
)