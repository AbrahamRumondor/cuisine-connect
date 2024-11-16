package com.example.cuisineconnect.app.listener

interface RecipeListListener {
  fun onRecipeClicked(recipeId: String)
  fun onRecipeLongClicked(recipeId: String)
  fun onItemDeleteClicked(itemId: String, type: String) {}
  fun onUserProfileClicked(userId: String) {}
}