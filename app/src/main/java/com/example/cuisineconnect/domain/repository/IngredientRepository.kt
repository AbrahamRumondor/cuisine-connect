package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Ingredient
import kotlinx.coroutines.flow.StateFlow

interface IngredientRepository {
    suspend fun getIngredients(): StateFlow<List<Ingredient>>
    fun getIngredientDocId(): String
    fun setIngredient(ingredientId: String, ingredientResponse: IngredientResponse)
}