package com.example.cuisineconnect.app.screen.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.auth.AuthUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ProfileViewModel @Inject constructor(
  @Named("db") private val db: FirebaseFirestore,
  private val userUseCase: UserUseCase,
  private val authUseCase: AuthUseCase,
  private val applicationContext: Context
) : ViewModel() {

  private val storageReference = FirebaseStorage.getInstance().reference

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  private val _otherUser: MutableStateFlow<User> = MutableStateFlow(User())
  val otherUser: StateFlow<User> = _otherUser

  init {
    getUser()
  }

  fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }

  fun getUser(userId: String) {
    viewModelScope.launch {
      val user = userUseCase.getUserByUserId(userId)
      user?.let {
        _otherUser.value = it
      }
    }
  }

  fun isAlreadyFollowing(): Boolean {
    return user.value.following.contains(otherUser.value.id)
  }

  fun updateUser (
    displayName: String,
    email: String,
    password: String,
    bio: String,
    imageProfileUri: Uri?,
    imageBackgroundUri: Uri?,
    result: () -> Unit
  ) {
    Log.d("ProfileViewModel", "bio: ${bio}")
    viewModelScope.launch {
      // Update user data in Firestore
      val updateUserSuccess = try {
      userUseCase.storeUser (
        user.value.id,
        user.value.copy(displayName = displayName, bio = bio)
      )
      true // Update successful
    } catch (e: Exception) {
      Log.e("ProfileViewModel", "Error updating user data: ${e.message}")
      Toast.makeText(applicationContext, "Failed to update user data", Toast.LENGTH_SHORT).show()
      false // Update failed
    }

      // If user data update is successful, proceed to update email and password
      // (Commented out for brevity, but you can include it as needed)

      // Handle image uploads
      val uploadTasks = mutableListOf<Deferred<Unit>>() // To keep track of upload tasks

      imageProfileUri?.let { uri ->
        uploadTasks.add(async {
          uploadImage(uri) { link ->
            if (link != null) {
              // Update the user's image link in Firestore
              db.collection("users").document(user.value.id)
                .update("user_image", link.toString())
                .addOnSuccessListener {
                  Log.d("ProfileViewModel", "User  image updated")
                }
                .addOnFailureListener { e ->
                  Log.e("ProfileViewModel", "User  image update failed: ${e.message}")
                }
            } else {
              Log.e("ProfileViewModel", "Image upload failed, link is null")
            }
          }
        })
      }

      imageBackgroundUri?.let { uri ->
        uploadTasks.add(async {
          uploadImage(uri) { link ->
            if (link != null) {
              // Update the user's background link in Firestore
              db.collection("users").document(user.value.id)
                .update("user_background", link.toString())
                .addOnSuccessListener {
                  Log.d("ProfileViewModel", "User  background updated")
                }
                .addOnFailureListener { e ->
                  Log.e("ProfileViewModel", "User  background update failed: ${e.message}")
                }
            } else {
              Log.e("ProfileViewModel", "Background image upload failed, link is null")
            }
          }
        })
      }

      // Await all uploads to complete
      uploadTasks.awaitAll()

      // Call the result callback after all updates are done
      result()
    }
  }

  private fun uploadImage(imageUri: Uri, result: (Uri?) -> Unit) {
    // Unique name for the image
    val fileName = UUID.randomUUID().toString()
    val ref = storageReference.child("images/$fileName")

    ref.putFile(imageUri)
      .addOnSuccessListener {
        // Get download URL and display it
        ref.downloadUrl.addOnSuccessListener { uri ->
          Log.d("FirebaseStorage", "Image URL: $uri")
          result(uri)
        }
      }
      .addOnFailureListener { e ->
        Log.e("FirebaseStorage", "Upload Failed", e)
        result(null)
      }
  }

  fun followUser(callback: TwoWayCallback) {
    userUseCase.followUser(user.value.id, otherUser.value.id, callback)
  }

  fun unFollowUser(callback: TwoWayCallback) {
    userUseCase.unfollowUser(user.value.id, otherUser.value.id, callback)
  }

}