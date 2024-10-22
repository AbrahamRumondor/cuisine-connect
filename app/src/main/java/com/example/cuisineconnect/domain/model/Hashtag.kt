package com.example.cuisineconnect.domain.model

import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.google.firebase.firestore.PropertyName

data class Hashtag(
  var id: String = "",
  var body: String = "",
  var timeStamps: Map<String, Int> = emptyMap(),
  var totalScore: Int = 0
)