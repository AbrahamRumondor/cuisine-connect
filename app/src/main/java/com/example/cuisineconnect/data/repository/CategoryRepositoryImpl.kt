package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.CategoryResponse
import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Category
import com.example.cuisineconnect.domain.repository.CategoryRepository
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class CategoryRepositoryImpl @Inject constructor(
  @Named("categoriesRef") private val categoriesRef: CollectionReference
) : CategoryRepository {

  private val _categories = MutableStateFlow<List<Category>>(emptyList())
  private val categories: StateFlow<List<Category>> = _categories

  override suspend fun getCategory(): StateFlow<List<Category>> {
    try {
      val snapshot = categoriesRef.get().await()
      val categoryList = snapshot.toObjects(CategoryResponse::class.java)
      _categories.value = categoryList.map { CategoryResponse.transform(it) }
    } catch (e: Exception) {
      _categories.value = emptyList()
    }
    return categories
  }

  override fun getCategoryDocId(): String {
    return categoriesRef.document().id
  }

  override fun setCategory(categoryId: String, categoryResponse: CategoryResponse) {
    categoriesRef.document(categoryId).set(categoryResponse).addOnSuccessListener {
      Timber.tag("TEST").d("SUCCESS ON category INSERTION")
    }
      .addOnFailureListener { Timber.tag("TEST").d("ERROR ON category INSERTION") }
  }
}