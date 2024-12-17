package com.example.cuisineconnect.app.util

sealed class Sort {
  object Latest : Sort() // Represents sorting by date
  object Oldest : Sort() // Represents sorting by most liked
  object MostLiked : Sort() // Represents sorting by most liked
  object LeastLiked : Sort() // Represents sorting by most liked
}