package com.example.cuisineconnect.domain.repository

import com.example.cuisineconnect.domain.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
  suspend fun getCurrentUser(uid: String): StateFlow<User>
  suspend fun storeUser(uid: String, user: User)
  fun addRecipeToUser(newRecipe: String)
}