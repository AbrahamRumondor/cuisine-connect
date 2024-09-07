package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Category
import com.google.firebase.firestore.PropertyName

data class CategoryResponse(
  @get:PropertyName("category_id")
  @set:PropertyName("category_id")
  var id: String = "",

  @get:PropertyName("category_body")
  @set:PropertyName("category_body")
  var body: String = "",

  @get:PropertyName("category_usages")
  @set:PropertyName("category_usages")
  var usages: List<String> = listOf(),
) {

  constructor() : this("")

  companion object {
    fun transform(categoryResponse: CategoryResponse): Category {
      return Category(
        id = categoryResponse.id,
        body = categoryResponse.body,
        usages = categoryResponse.usages
      )
    }

    fun transform(category: Category): CategoryResponse {
      return CategoryResponse(
        id = category.id,
        body = category.body,
        usages = category.usages
      )
    }
  }
}