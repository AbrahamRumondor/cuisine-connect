package com.example.cuisineconnect.app.listener

import com.example.cuisineconnect.domain.model.Post

interface ItemListListener {
  fun onRecipeClicked(recipeId: String) {}
  fun onRecipeLongClicked(recipeId: String) {}
  fun onItemDeleteClicked(itemId: String, type: String) {}
  fun onPostClicked(postId: String) {}
  fun onUserProfileClicked(userId: String) {}
}