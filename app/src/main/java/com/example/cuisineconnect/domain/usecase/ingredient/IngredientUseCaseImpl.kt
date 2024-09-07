package com.example.cuisineconnect.domain.usecase.ingredient

import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.domain.model.Ingredient
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.repository.IngredientRepository
import com.example.cuisineconnect.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class IngredientUseCaseImpl @Inject constructor(
  private val ingredientRepository: IngredientRepository
) : IngredientUseCase {

  override suspend fun getIngredients(): StateFlow<List<Ingredient>> {
    return ingredientRepository.getIngredients()
  }

  override fun getIngredientDocId(): String {
    return ingredientRepository.getIngredientDocId()
  }

  override fun setIngredient(ingredientId: String, ingredientResponse: IngredientResponse) {
    ingredientRepository.setIngredient(ingredientId, ingredientResponse)
  }

  // TODO JANGAN LUPA KASIH DELAY 1-2s
  //  so it will only trigger when user stop typing
  override fun getIngredientsByText(
    text: String,
    allIngredients: List<Ingredient>
  ): List<Ingredient> {
    return allIngredients
      .filter { ingredient ->
        ingredient.body.contains(text, ignoreCase = true)
      }
      .sortedByDescending { ingredient ->
        ingredient.usages.size
      }
  }

}