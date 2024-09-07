package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.IngredientResponse
import com.example.cuisineconnect.domain.model.Ingredient
import com.example.cuisineconnect.domain.repository.IngredientRepository
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class IngredientRepositoryImpl @Inject constructor(
  @Named("ingredientsRef") private val ingredientsRef: CollectionReference
) : IngredientRepository {

  private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
  private val ingredients: StateFlow<List<Ingredient>> = _ingredients

  override suspend fun getIngredients(): StateFlow<List<Ingredient>> {
    try {
      val snapshot = ingredientsRef.get().await()
      val ingredientList = snapshot.toObjects(IngredientResponse::class.java)
      _ingredients.value = ingredientList.map { IngredientResponse.transform(it) }
    } catch (e: Exception) {
      _ingredients.value = emptyList()
    }
    return ingredients
  }

  override fun getIngredientDocId(): String {
    return ingredientsRef.document().id
  }

  override fun setIngredient(ingredientId: String, ingredientResponse: IngredientResponse) {
    ingredientsRef.document(ingredientId).set(ingredientResponse).addOnSuccessListener {
      Timber.tag("TEST").d("SUCCESS ON ingredient INSERTION")
    }
      .addOnFailureListener { Timber.tag("TEST").d("ERROR ON ingredient INSERTION") }
  }
}