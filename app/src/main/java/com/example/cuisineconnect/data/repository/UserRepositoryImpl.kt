package com.example.cuisineconnect.data.repository

import android.util.Log
import com.example.cuisineconnect.data.response.UserResponse
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class UserRepositoryImpl @Inject constructor(
  @Named("usersRef") private val usersRef: CollectionReference
) : UserRepository {

  private val _currentUser = MutableStateFlow(User())
  private val currentUser: StateFlow<User> = _currentUser

  override suspend fun getCurrentUser(uid: String): StateFlow<User> {
    try {
      val snapshot = usersRef.get().await()
      val user = snapshot.toObjects(UserResponse::class.java)
        .find { it.id == uid }
        ?.let { UserResponse.transform(it) }

      if (user != null) {
        _currentUser.value = user
      }
    } catch (e: Exception) {
      _currentUser.value = User()
    }
    return currentUser
  }

  override suspend fun getUserByUserId(userId: String): User? {
    return try {
      val snapshot = usersRef.document(userId).get().await()
      val user = snapshot.toObject(UserResponse::class.java)?.let { UserResponse.transform(it) }

      user
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun storeUser(uid: String, user: User) {
    val currentUser = usersRef.document(uid)

    try {
      currentUser.set(UserResponse.transform(user))
    } catch (
      e: Exception
    ) {
      Timber.tag("UserRepositoryImpl").e(
        e, "Error saving user"
      )
    }
  }

  override fun addRecipeToUser(newRecipe: String) {
    usersRef.document(currentUser.value.id)
      .update("user_recipes", FieldValue.arrayUnion(newRecipe))
      .addOnSuccessListener {
        // Handle success
        println("Recipe added successfully")
      }
      .addOnFailureListener { e ->
        // Handle failure
        println("Error adding recipe: ${e.message}")
      }
  }

  override fun addPostToUser(newPost: String) {
    usersRef.document(currentUser.value.id)
      .update("user_posts", FieldValue.arrayUnion(newPost))
      .addOnSuccessListener {
        // Handle success
        println("Recipe added successfully")
      }
      .addOnFailureListener { e ->
        // Handle failure
        println("Error adding recipe: ${e.message}")
      }
  }

  override suspend fun getUsersStartingWith(prefix: String): List<User> {
    return try {
      val querySnapshot = usersRef
        .orderBy("user_name")
        .startAt(prefix)
        .endAt(prefix + "\uf8ff") // This ensures we get all names starting with the prefix
        .get()
        .await()

      // Map the results to the User model
      querySnapshot.toObjects(UserResponse::class.java)
        .map {
          Log.d("UserRepositoryImpl", it.toString())
          UserResponse.transform(it)
        }
    } catch (e: Exception) {
      Timber.tag("UserRepositoryImpl").e(e, "Error fetching users with prefix: $prefix")
      emptyList()
    }
  }

}
