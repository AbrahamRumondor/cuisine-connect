package com.example.cuisineconnect.app.component

interface RecipeListListener {
  fun onRecipeClicked(position: Int, recipeId: String)
}