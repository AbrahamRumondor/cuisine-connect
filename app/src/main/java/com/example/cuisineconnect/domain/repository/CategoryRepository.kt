package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.data.response.CategoryResponse
import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Category
import com.example.cuisineconnect.domain.model.Ingredient
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {
    suspend fun getCategory(): StateFlow<List<Category>>
    fun getCategoryDocId(): String
    fun setCategory(categoryId: String, categoryResponse: CategoryResponse)
}