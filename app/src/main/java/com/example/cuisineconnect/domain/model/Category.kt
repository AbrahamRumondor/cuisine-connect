package com.example.cuisineconnect.domain.model

import com.example.cuisineconnect.data.response.IngredientResponse

data class Category(
  var id: String = "",
  var body: String = "",
  var usages: List<String> = listOf(),
)