package com.example.cuisineconnect.domain.usecase.category

import com.example.cuisineconnect.data.response.CategoryResponse
import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Category
import com.example.cuisineconnect.domain.model.Ingredient
import kotlinx.coroutines.flow.StateFlow

interface CategoryUseCase {
  suspend fun getCategories(): StateFlow<List<Category>>
  fun getCategoryDocId(): String
  fun setCategory(categoryId: String, categoryResponse: CategoryResponse)
  fun getCategoriesByText(
    text: String,
    allCategories: List<Category>
  ): List<Category>
}