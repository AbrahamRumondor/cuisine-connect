package com.example.cuisineconnect.domain.usecase.ingredient

import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Ingredient
import kotlinx.coroutines.flow.StateFlow

interface IngredientUseCase {
  suspend fun getIngredients(): StateFlow<List<Ingredient>>
  fun getIngredientDocId(): String
  fun setIngredient(ingredientId: String, ingredientResponse: IngredientResponse)
  fun getIngredientsByText(text: String, allIngredients: List<Ingredient>): List<Ingredient>
}