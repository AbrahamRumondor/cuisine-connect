package com.example.cuisineconnect.data.repository

import android.util.Log
import com.example.cuisineconnect.data.response.UserResponse
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.UserRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
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

  private val _userBookmarks = MutableStateFlow<List<String>>(emptyList())
  private val userBookmarks: StateFlow<List<String>> = _userBookmarks

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

  override suspend fun storeUser(uid: String, user: User, isUpdate: Boolean): Result<Unit> {
    val currentUser = usersRef.document(uid)

    return try {
      // Only check for unique username if this is not an update
      if (!isUpdate) {
        val uniqueNameQuery = usersRef.whereEqualTo("user_unique_name", user.username).get()
        val result = uniqueNameQuery.await()

        if (!result.isEmpty && result.documents[0].id != uid) {
          // If another document with the same unique name exists
          val errorMessage = "We're sorry, '${user.username}' is already taken."
          Timber.tag("UserRepositoryImpl").e(errorMessage)
          return Result.failure(Exception(errorMessage)) // Return failure with custom error message
        }
      }

      // If no document with the same unique name exists or it's an update
      currentUser.set(UserResponse.transform(user)).await()
      Result.success(Unit) // Return success
    } catch (e: Exception) {
      if (!isUpdate && e.message?.contains("is already taken") == true) {
        Result.failure(e) // Return only the specific error for unique username issues
      } else {
        Timber.tag("UserRepositoryImpl").e(e, "Error saving user")
        Result.failure(Exception("An error occurred while saving the user.")) // General error for other issues
      }
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

  override fun followUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback) {
    // Add targetUserId to the current user's following list
    usersRef.document(currentUserId)
      .update("user_following", FieldValue.arrayUnion(targetUserId))
      .addOnSuccessListener {
        // Add currentUserId to the target user's follower list
        usersRef.document(targetUserId)
          .update("user_follower", FieldValue.arrayUnion(currentUserId))
          .addOnSuccessListener {
            callback.onSuccess()  // Trigger success callback
          }
          .addOnFailureListener { e ->
            callback.onFailure("Error adding follower: ${e.message}")  // Trigger failure callback
          }
      }
      .addOnFailureListener { e ->
        callback.onFailure("Error following user: ${e.message}")  // Trigger failure callback
      }
  }

  override fun unfollowUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback) {
    // Remove targetUserId from the current user's following list
    usersRef.document(currentUserId)
      .update("user_following", FieldValue.arrayRemove(targetUserId))
      .addOnSuccessListener {
        // Remove currentUserId from the target user's follower list
        usersRef.document(targetUserId)
          .update("user_follower", FieldValue.arrayRemove(currentUserId))
          .addOnSuccessListener {
            callback.onSuccess()  // Trigger success callback
          }
          .addOnFailureListener { e ->
            callback.onFailure("Error removing follower: ${e.message}")  // Trigger failure callback
          }
      }
      .addOnFailureListener { e ->
        callback.onFailure("Error unfollowing user: ${e.message}")  // Trigger failure callback
      }
  }

  override suspend fun getUsersStartingWith(prefix: String): List<User> {
    return try {
      // Check if the prefix starts with '@' and determine the field and searchPrefix accordingly
      val (field, searchPrefix) = if (prefix.startsWith("@")) {
        "user_unique_name" to prefix.substring(1) // Remove the '@' from prefix
      } else {
        "user_name" to prefix
      }

      // Perform the query based on the selected field and search prefix
      val querySnapshot = usersRef
        .orderBy(field)
        .startAt(searchPrefix)
        .endAt(searchPrefix + "\uf8ff") // Ensures fetching all names starting with the prefix
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

  override suspend fun getUserBookmarks(userId: String): StateFlow<List<String>> {
    try {
      val snapshot = usersRef.document(userId).get().await()
      val userResponse = snapshot.toObject(UserResponse::class.java)

      // Update the state with the bookmarks list or empty list if null
      _userBookmarks.value = userResponse?.bookmarks ?: emptyList()
    } catch (e: Exception) {
      Timber.tag("UserRepositoryImpl").e(e, "Error fetching bookmarks for user: $userId")
      _userBookmarks.value = emptyList()
    }
    return userBookmarks
  }

}
