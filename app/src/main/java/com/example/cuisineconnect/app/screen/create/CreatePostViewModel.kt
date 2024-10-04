package com.example.cuisineconnect.app.screen.create

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.PostResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.post.PostUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
  private val userUseCase: UserUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val postUseCase: PostUseCase
) : ViewModel() {
  private val storageReference = FirebaseStorage.getInstance().reference

  val postContent: MutableList<MutableMap<String, String>> = mutableListOf()
  val imageList: MutableList<Uri> = mutableListOf()

  private val _user: MutableStateFlow<User> = MutableStateFlow(User())
  val user: StateFlow<User> = _user

  init {
    getUser()
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

  fun getRecipeById(recipeId: String, result: (Pair<User, Recipe>) -> Unit) {
    viewModelScope.launch {
      // Fetch the recipe
      val recipe = withContext(Dispatchers.IO) {
        recipeUseCase.getRecipeByID(recipeId)
      }

      // If recipe is not null, fetch the user
      val user = recipe?.let {
        val userId = it.id.substringBefore("_")
        withContext(Dispatchers.IO) {
          userUseCase.getUserByUserId(userId)
        }
      }

      // Set the value of _recipes if both recipe and user are available
      recipe?.let { r ->
        user?.let { u ->
          result(Pair(u, r))
        }
      }
    }
  }

  fun savePostInDatabase(result: () -> Unit) {
    viewModelScope.launch {
      val id = createPostDoc()

      // Upload all images in postContent and update the "value" with the URL
      val updatedPostContent = postContent.toMutableList()

      updatedPostContent.forEachIndexed { index, item ->
        when (item["type"]) {
          "image" -> {
            // Find the corresponding image URI from imageList based on the "value" field
            val imageUriString = item["value"]
            val imageUri = imageList.find { it.toString() == imageUriString }

            imageUri?.let { uri ->
              uploadImage(uri) { uploadedUri ->
                uploadedUri?.let { downloadUri ->
                  // Update the postContent with the new Firebase URL
                  updatedPostContent[index] = mutableMapOf(
                    "type" to "image",
                    "value" to downloadUri.toString()
                  )
                }
              }
            }
          }
        }
      }

      // Ensure that all image uploads and post content updates are completed before saving
//      val noneEmpty = updatedPostContent.all { map -> map.values.none { it.isEmpty() } }
//      if (noneEmpty) {
      val post = PostResponse().copy(
        id = id,
        date = Date(),
        postContent = updatedPostContent
      )

      postUseCase.setPost(id, post, result)
//      }
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