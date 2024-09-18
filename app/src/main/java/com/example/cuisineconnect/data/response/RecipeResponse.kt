package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Recipe
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class RecipeResponse(
  @get:PropertyName("recipe_id")
  @set:PropertyName("recipe_id")
  var id: String = "", // userId_recipeId

  @get:PropertyName("recipe_title")
  @set:PropertyName("recipe_title")
  var title: String = "",

  @get:PropertyName("recipe_ingredient")
  @set:PropertyName("recipe_ingredient")
  var ingredients: List<String> = listOf(),

  @get:PropertyName("recipe_date")
  @set:PropertyName("recipe_date")
  var date: Date = Date(),

  @get:PropertyName("recipe_upvotes")
  @set:PropertyName("recipe_upvotes")
  var upvotes: Map<String, Boolean> = emptyMap(),

  @get:PropertyName("recipe_category")
  @set:PropertyName("recipe_category")
  var category: List<String> = listOf(),

  @get:PropertyName("recipe_image")
  @set:PropertyName("recipe_image")
  var image: String = "",

  @get:PropertyName("recipe_description")
  @set:PropertyName("recipe_description")
  var description: String = "",

  @get:PropertyName("recipe_portion")
  @set:PropertyName("recipe_portion")
  var portion: Int = 0,

  @get:PropertyName("recipe_duration")
  @set:PropertyName("recipe_duration")
  var duration: Int = 0,
) {

  constructor() : this("")

  companion object {
    fun transform(recipeResponse: RecipeResponse): Recipe {
      return Recipe(
        id = recipeResponse.id,
        title = recipeResponse.title,
        ingredients = recipeResponse.ingredients,
        date = recipeResponse.date,
        upvotes = recipeResponse.upvotes,
        category = recipeResponse.category,
        image = recipeResponse.image,
        description = recipeResponse.description,
        portion = recipeResponse.portion,
        duration = recipeResponse.duration
      )
    }

    fun transform(recipe: Recipe): RecipeResponse {
      return RecipeResponse(
        id = recipe.id,
        title = recipe.title,
        ingredients = recipe.ingredients,
        date = recipe.date,
        upvotes = recipe.upvotes,
        category = recipe.category,
        image = recipe.image,
        description = recipe.description,
        portion = recipe.portion,
        duration = recipe.duration
      )
    }
  }
}