package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.UserResponse
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.UserRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
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

}
