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

  @get:PropertyName("recipe_hashtags")
  @set:PropertyName("recipe_hashtags")
  var hashtags: List<String> = emptyList(),

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

  @get:PropertyName("recipe_reply_count")
  @set:PropertyName("recipe_reply_count")
  var replyCount: Int = 0,

  @get:PropertyName("recipe_bookmarks")
  @set:PropertyName("recipe_bookmarks")
  var bookmarks: Map<String, Boolean> = emptyMap(),

  @get:PropertyName("recipe_referenced_by")
  @set:PropertyName("recipe_referenced_by")
  var referencedBy: Map<String, Boolean> = emptyMap(),

  @get:PropertyName("recipe_user_id")
  @set:PropertyName("recipe_user_id")
  var userId: String = "",

  @get:PropertyName("recipe_title_split")
  @set:PropertyName("recipe_title_split")
  var recipeTitleSplit: List<String> = listOf()
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
        hashtags = recipeResponse.hashtags,
        image = recipeResponse.image,
        description = recipeResponse.description,
        portion = recipeResponse.portion,
        duration = recipeResponse.duration,
        bookmarks = recipeResponse.bookmarks,
        replyCount = recipeResponse.replyCount,
        referencedBy = recipeResponse.referencedBy,
        userId = recipeResponse.userId,
        recipeTitleSplit = recipeResponse.recipeTitleSplit
      )
    }

    fun transform(recipe: Recipe): RecipeResponse {
      return RecipeResponse(
        id = recipe.id,
        title = recipe.title,
        ingredients = recipe.ingredients,
        date = recipe.date,
        upvotes = recipe.upvotes,
        hashtags = recipe.hashtags,
        image = recipe.image,
        description = recipe.description,
        portion = recipe.portion,
        duration = recipe.duration,
        bookmarks = recipe.bookmarks,
        replyCount = recipe.replyCount,
        referencedBy = recipe.referencedBy,
        userId = recipe.userId,
        recipeTitleSplit = recipe.recipeTitleSplit
      )
    }
  }
}