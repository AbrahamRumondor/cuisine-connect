package com.example.cuisineconnect.app.screen.create

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.callbacks.FirestoreCallback
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.hashtag.HashtagUseCase
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.step.StepUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(
  @Named("db") private val db: FirebaseFirestore,
  private val stepUseCase: StepUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase,
  private val hashtagUseCase: HashtagUseCase,
  private val applicationContext: Context
) : ViewModel() {

  private val storageReference = FirebaseStorage.getInstance().reference

  fun saveRecipeProgress(recipeId: String, recipeResponse: RecipeResponse,callback: TwoWayCallback) {
    userUseCase.saveRecipeContentForCurrentUser(recipeId, recipeResponse, callback)
  }

  fun deleteRecipeProgress(callback: TwoWayCallback) {
    userUseCase.clearRecipeContentForCurrentUser(callback)
  }

  fun fetchSavedRecipeContent(callback: (Map<String, Any>?) -> Unit) {
    viewModelScope.launch {
      userUseCase.fetchRecipeContentForCurrentUser(callback)
    }
  }

  fun fetchSavedRecipeSteps(recipeId: String, callback: (List<Step>) -> Unit) {
    viewModelScope.launch {
      currentUser?.let { user ->
        stepUseCase.getSteps(user.id, recipeId).collectLatest {
          callback(it)
        }
      }
    }
  }

  fun toSteps(
    body: List<String>
  ): List<Step> {
    // TODO ADD IMAGE
    return body.mapIndexed { index, bodyContent ->
      Step(
        no = index + 1,
        body = bodyContent,
      )
    }
  }

  fun saveRecipeInDatabase(
    title: String,
    description: String,
    portion: String,
    duration: String,
    image: String,
    steps: List<Step>,
    ingredients: List<String>,
    imageUri: Uri?,
    hashtags: List<String>,
    onResult: (msg: Int?) -> Unit
  ) {
    getUserFromDB(object : FirestoreCallback {
      override fun onSuccess(user: User?) {

        user?.let { me ->
          try {
            val recipeId = getRecipeDocumentId(me.id)

            db.runTransaction { transaction ->
              val recipe = Recipe(
                id = recipeId,
                userId = me.id,
                title = title,
                description = description,
                portion = portion,
                duration = duration,
                image = image,
                ingredients = ingredients,
                date = Date(),
                referencedBy = mapOf(me.id to true),
                hashtags = hashtags,
                recipeTitleSplit = title.split(" ").map { it.lowercase() }
              )
              val recipeToFirebase = RecipeResponse.transform(recipe)

              // Save Recipe in Firestore
              transaction.set(
                db.collection("recipes").document(recipe.id), recipeToFirebase
              )

              // Update reference to steps collection under the recipe
              val stepRefs = steps.map { step ->
                val newStep = step.copy(id = getRecipeStepDocumentId(recipe.id))
                db.collection("recipes").document(recipe.id)
                  .collection("steps").document(newStep.id) to newStep
              }

              // Save Steps
              stepRefs.forEachIndexed { index, (ref, step) ->
                val stepResponse = StepResponse.transform(step)
                transaction.set(ref, stepResponse)
              }

              userUseCase.addRecipeToUser(recipeId)

            }.addOnSuccessListener {
              imageUri?.let { uri ->
                uploadImage(uri) { link ->
                  if (link != null) {
                    // Update the recipe document with the image link
                    db.collection("recipes").document(recipeId)
                      .update("recipe_image", link.toString())
                      .addOnSuccessListener {
                        Log.d("createRecipeViewModel", "Recipe image updated")
                      }
                      .addOnFailureListener { e ->
                        Timber.tag("createRecipeViewModel").d("Image update failed: %s", e.message)
                      }
                  } else {
                    // Handle image upload failure
                    onResult(R.string.image_upload_failed)
                  }
                }
              }

              if (hashtags.isNotEmpty()) {
                hashtags.forEach {
                  addNewHashtag(it, recipeId)
                }
              }

              Log.d("createRecipeViewModel", "success")
              onResult(null)
            }.addOnFailureListener { e ->
              Timber.tag("createRecipeViewModel").d("transaction failed: %s", e.message)
              onResult(R.string.save_failed)
            }
          } catch (e: Exception) {
            Timber.tag("createRecipeViewModel")
              .d("transaction failed: %s", e.message)
            onResult(R.string.save_failed_2)
          }
        }
      }

      override fun onFailure(exception: Exception) {
        onResult(R.string.save_failed_user)
      }
    })
  }

  fun saveRecipeProgressForCurrentUser(
    title: String,
    description: String,
    portion: String,
    duration: String,
    image: String,
    steps: List<Step>,
    ingredients: List<String>,
    imageUri: Uri?,
    hashtags: List<String>,
    existId: String?,
    onResult: (msg: Int?) -> Unit
  ) {
    getUserFromDB(object : FirestoreCallback {
      override fun onSuccess(user: User?) {
        user?.let { me ->
          try {
            Log.d("createRecipeViewModel", "inge: ${ingredients}")
            Log.d("createRecipeViewModel", "step: ${steps}")

            val recipeId = if (!existId.isNullOrEmpty()) existId else getRecipeDocumentId(me.id)

            // Create the Recipe object
            val recipe = Recipe(
              id = recipeId,
              userId = me.id,
              title = title,
              description = description,
              portion = portion,
              duration = duration,
              image = image,
              ingredients = ingredients,
              date = Date(),
              referencedBy = mapOf(me.id to true),
              hashtags = hashtags,
              recipeTitleSplit = title.split(" ").map { it.lowercase() }
            )
            val recipeToFirebase = RecipeResponse.transform(recipe)

            // Fetch existing steps first (no need for a coroutine)
            val userRecipeRef = db.collection("users")
              .document(me.id)
              .collection("recipes")
              .document(recipeId)
            val stepsRef = userRecipeRef.collection("steps")

            stepsRef.get().addOnSuccessListener { existingStepsSnapshot ->
              // Transaction to update recipe and steps
              db.runTransaction { transaction ->
                // Delete the existing recipe document
                transaction.delete(userRecipeRef)

                // Set the new recipe document
                transaction.set(userRecipeRef, recipeToFirebase)

                // Delete existing steps under the Recipe's "steps" subcollection
                existingStepsSnapshot.documents.forEach { document ->
                  transaction.delete(document.reference) // Delete each step
                }

                // Save the new Steps under the Recipe's "steps" subcollection
                steps.forEach { step ->
                  val newStep = step.copy(id = getRecipeStepDocumentId(recipeId))
                  val stepRef = userRecipeRef.collection("steps").document(newStep.id)
                  transaction.set(stepRef, StepResponse.transform(newStep)) // Add new steps
                }
              }.addOnSuccessListener {
                // After transaction, handle image upload if there's an image
                imageUri?.let { uri ->
                  uploadImage(uri) { link ->
                    if (link != null) {
                      // Update the recipe document with the image link after upload success
                      db.collection("users")
                        .document(me.id)
                        .collection("recipes")
                        .document(recipeId)
                        .update("recipe_image", link.toString())
                        .addOnSuccessListener {
                          Log.d("createRecipeViewModel", "Recipe image updated")
                        }
                        .addOnFailureListener { e ->
                          Timber.tag("createRecipeViewModel").d("Image update failed: %s", e.message)
                        }
                    } else {
                      // Handle image upload failure
                      onResult(R.string.image_upload_failed)
                    }
                  }
                }

//                // Handle hashtags
//                if (hashtags.isNotEmpty()) {
//                  hashtags.forEach { hashtag ->
//                    addNewHashtag(hashtag, recipeId)
//                  }
//                }

                // Now save the recipe progress using your method
                saveRecipeProgress(recipeId, recipeToFirebase, object : TwoWayCallback {
                  override fun onSuccess() {
                    Log.d("createRecipeViewModel", "success save progress")
                  }

                  override fun onFailure(errorMessage: String) {
                    Log.d("createRecipeViewModel", "failed save progress: ${errorMessage}")
                    onResult(R.string.save_failed)
                  }
                })

                // Success callback
                Log.d("createRecipeViewModel", "success")
                onResult(null)
              }.addOnFailureListener { e ->
                Timber.tag("createRecipeViewModel").d("Transaction failed: %s", e.message)
                onResult(R.string.save_failed)
              }
            }.addOnFailureListener { e ->
              Timber.tag("createRecipeViewModel").d("Failed to get existing steps: %s", e.message)
              onResult(R.string.save_failed_2)
            }
          } catch (e: Exception) {
            Timber.tag("createRecipeViewModel").d("Transaction failed: %s", e.message)
            onResult(R.string.save_failed_2)
          }
        } ?: onResult(R.string.save_failed_user)
      }

      override fun onFailure(exception: Exception) {
        Timber.tag("createRecipeViewModel").d("User fetch failed: %s", exception.message)
        onResult(R.string.save_failed_user)
      }
    })
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


  private fun getUserFromDB(callback: FirestoreCallback) {
    viewModelScope.launch {
      try {
        val user = userUseCase.getCurrentUser()
        callback.onSuccess(user.value)
      } catch (e: Exception) {
        callback.onFailure(e)
      }
    }
  }

  fun getRecipeDocumentId(userId: String): String {
    return recipeUseCase.getRecipeDocID(userId)
  }

  private fun getRecipeStepDocumentId(recipeId: String): String {
    return recipeUseCase.getRecipeStepDocID(recipeId)
  }

  fun addNewHashtag(tag: String, itemId: String) {
    val currentTimeMillis = System.currentTimeMillis()
    val flooredTimeMillis = floorToHour(currentTimeMillis) // Round to the nearest hour

    hashtagUseCase.addHashtag(tag.lowercase(), itemId, flooredTimeMillis, 1) { success, exception ->
      if (success) {
        Toast.makeText(applicationContext, applicationContext.getString(R.string.hashtag_added_successfully), Toast.LENGTH_SHORT).show()
      } else {
        // Handle the failure case, such as showing an error message
        exception?.let {
          Toast.makeText(
            applicationContext,
            "Failed to add hashtag: ${it.message}",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  private fun floorToHour(timeInMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis // Use the setter method to update the time
    calendar.set(Calendar.MINUTE, 0)  // Set the minute to 0
    calendar.set(Calendar.SECOND, 0)  // Set the second to 0
    calendar.set(Calendar.MILLISECOND, 0)  // Set the millisecond to 0

    return calendar.timeInMillis // Return the floored time in milliseconds
  }

  fun searchTags(query: String, callback: (List<Hashtag>, Exception?) -> Unit) {
    hashtagUseCase.searchHashtags(query, callback)
  }

}