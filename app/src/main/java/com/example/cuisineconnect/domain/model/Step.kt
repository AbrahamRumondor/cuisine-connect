package com.example.cuisineconnect.domain.model

data class Step(
  var id: String = "",
  var no: Int = 0,
  var body: String = "",
  var images: List<String> = listOf(),
)