package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Ingredient
import com.google.firebase.firestore.PropertyName

data class IngredientResponse(
  @get:PropertyName("ingredient_id")
  @set:PropertyName("ingredient_id")
  var id: String = "",

  @get:PropertyName("ingredient_body")
  @set:PropertyName("ingredient_body")
  var body: String = "",

  @get:PropertyName("ingredient_usages")
  @set:PropertyName("ingredient_usages")
  var usages: List<String> = listOf(),
) {

  constructor() : this("")

  companion object {
    fun transform(ingredientResponse: IngredientResponse): Ingredient {
      return Ingredient(
        id = ingredientResponse.id,
        body = ingredientResponse.body,
        usages = ingredientResponse.usages
      )
    }

    fun transform(ingredient: Ingredient): IngredientResponse {
      return IngredientResponse(
        id = ingredient.id,
        body = ingredient.body,
        usages = ingredient.usages
      )
    }
  }
}