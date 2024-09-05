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
  var upvote: Int = 0,
  var image: String = "",
)