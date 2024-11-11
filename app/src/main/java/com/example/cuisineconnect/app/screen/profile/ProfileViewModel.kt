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

  fun updateUser(
    username: String,
    email: String,
    password: String,
    imageUri: Uri?,
    result: () -> Unit
  ) {
    viewModelScope.launch {
      // Update user data in Firestore
      val updateUserSuccess = try {
        userUseCase.storeUser(
          user.value.id,
          user.value.copy(displayName = username)
        )
        true // Update successful
      } catch (e: Exception) {
        Log.e("ProfileViewModel", "Error updating user data: ${e.message}")
        Toast.makeText(applicationContext, "Failed to update user data", Toast.LENGTH_SHORT).show()
        false // Update failed
      }

      // If user data update is successful, proceed to update email and password
//      if (updateUserSuccess) {
//        val reAuthResult = authUseCase.reAuthenticateUser(user.value.email, user.value.password)
//
//        if (reAuthResult.isSuccess) {
//          val emailUpdateSuccess = authUseCase.updateEmail(email).isSuccess
//          val passwordUpdateSuccess = authUseCase.updatePassword(password).isSuccess
//
//          if (emailUpdateSuccess && passwordUpdateSuccess) {
//            Toast.makeText(applicationContext, "Success updating profile", Toast.LENGTH_SHORT)
//              .show()
//          } else {
//            Log.e("ProfileViewModel", "Failed to update email or password")
//            Toast.makeText(
//              applicationContext,
//              "Failed to update email or password",
//              Toast.LENGTH_SHORT
//            ).show()
//          }
//        } else {
//          Log.e("ProfileViewModel", "Re-authentication failed")
//          Toast.makeText(applicationContext, "Re-authentication required", Toast.LENGTH_SHORT)
//            .show()
//        }
//      }

      // Handle image upload if imageUri is provided
      imageUri?.let { uri ->
        uploadImage(uri) { link ->
          if (link != null) {
            // Update the user's image link in Firestore
            db.collection("users").document(user.value.id)
              .update("user_image", link.toString())
              .addOnSuccessListener {
                result()
                Log.d("profileViewModel", "User image updated")
              }
              .addOnFailureListener { e ->
                Timber.tag("profileViewModel").d("User update failed: %s", e.message)
              }
          } else {
            Log.e("ProfileViewModel", "Image upload failed, link is null")
          }
        }
      } ?: result()
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