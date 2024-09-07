package com.example.cuisineconnect.domain.usecase.category

import com.example.cuisineconnect.data.response.CategoryResponse
import com.example.cuisineconnect.domain.model.Category
import com.example.cuisineconnect.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class CategoryUseCaseImpl @Inject constructor(
  private val categoryRepository: CategoryRepository
) : CategoryUseCase {

  override suspend fun getCategories(): StateFlow<List<Category>> {
    return categoryRepository.getCategory()
  }

  override fun getCategoryDocId(): String {
    return categoryRepository.getCategoryDocId()
  }

  override fun setCategory(categoryId: String, categoryResponse: CategoryResponse) {
    categoryRepository.setCategory(categoryId, categoryResponse)
  }

  // TODO JANGAN LUPA KASIH DELAY 1-2s
  //  so it will only trigger when user stop typing
  override fun getCategoriesByText(
    text: String,
    allCategories: List<Category>
  ): List<Category> {
    return allCategories
      .filter { category ->
        category.body.contains(text, ignoreCase = true)
      }
      .sortedByDescending { category ->
        category.usages.size
      }
  }

}