package com.example.cuisineconnect.app.screen.create

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class CreatePostViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {
  private val storageReference = FirebaseStorage.getInstance().reference

  var postContent: MutableList<MutableMap<String, String>> = mutableListOf()
  val imageList: MutableList<Uri> = mutableListOf()

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
  }

  fun savePostProgress(callback: TwoWayCallback) {
    userUseCase.savePostContentForCurrentUser(postContent, callback)
  }

  fun deletePostProgress(callback: TwoWayCallback) {
    userUseCase.clearPostContentForCurrentUser(callback)
  }

  fun fetchSavedPostContent(result: () -> Unit) {
    viewModelScope.launch {
      userUseCase.fetchPostContentForCurrentUser {
        it.onSuccess { content ->
          Log.d("lololol", "this vm: ${content}")
          postContent = content
          result()
        }
        it.onFailure {
          Log.d("lololol", "this vm: ${it}")
        }
      }
    }
  }

  private fun getUser() {
    viewModelScope.launch {
      _user.value = userUseCase.getCurrentUser().value
      currentUser = _user.value
    }
  }

  fun clearPostContents() {
    postContent.clear()
    imageList.clear()
  }

  fun getRecipeById(recipeId: String, result: (Pair<User, Recipe>?) -> Unit) {
    viewModelScope.launch {
      // Fetch the recipe
      val recipe = withContext(Dispatchers.IO) {
        recipeUseCase.getRecipeByID(recipeId)
      }

      // If recipe is not null, fetch the user
      val user = recipe?.let {
        val userId = it.id.substringAfter("_").substringBefore("_")
        withContext(Dispatchers.IO) {
          userUseCase.getUserByUserId(userId)
        }
      }

      // Set the value of _recipes if both recipe and user are available
      recipe?.let { r ->
        user?.let { u ->
          result(Pair(u, r))
        }
      } ?: result(null)
    }
  }

  fun savePostInDatabase(result: () -> Unit) {
    if (postContent.isEmpty()) {
      result()
      return
    }
    viewModelScope.launch {
      val id = createPostDoc()

      // Upload all images in postContent and update the "value" with the URL
      val updatedPostContent = postContent.toMutableList()

      withContext(Dispatchers.IO) {
        // Sequentially upload images and update postContent
        updatedPostContent.forEachIndexed { index, item ->
          val types = item["type"]
          types?.let { type ->
            if (type.contains("image")) {
              val imageUriString = item["value"]
              val imageUri = imageList.find { it.toString() == imageUriString }

              imageUri?.let { uri ->
                // Await the image upload and update the postContent
                val uploadedUri = uploadImageSuspend(uri)
                uploadedUri?.let { downloadUri ->
                  updatedPostContent[index] = mutableMapOf(
                    "type" to type,
                    "value" to downloadUri.toString()
                  )
                }
              }
            }
          }
        }
      }

      // Ensure that all image uploads and post content updates are completed before saving
      val post = PostResponse().copy(
        id = id,
        userId = user.value.id,
        date = Date(),
        postContent = updatedPostContent,
        referencedBy = mapOf(user.value.id to true)
      )

      postUseCase.setPost(id, post, result)
      userUseCase.addPostToUser(post.id)
    }
  }

  private suspend fun uploadImageSuspend(uri: Uri): Uri? {
    return withContext(Dispatchers.IO) {
      suspendCancellableCoroutine { continuation ->
        uploadImage(uri) { uploadedUri ->
          if (continuation.isActive) {
            continuation.resume(uploadedUri)
          } else {
            continuation.resume(null)
          }
        }
      }
    }
  }
  private fun createPostDoc(): String {
    return postUseCase.getPostDocID(user.value.id)
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
}