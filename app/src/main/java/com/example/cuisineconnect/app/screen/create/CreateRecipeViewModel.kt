package com.example.cuisineconnect.app.screen.create

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.alfaresto_customersapp.domain.callbacks.FirestoreCallback
import com.example.cuisineconnect.R
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.StepResponse
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.usecase.recipe.RecipeUseCase
import com.example.cuisineconnect.domain.usecase.step.StepUseCase
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(
  @Named("db") private val db: FirebaseFirestore,
  private val stepUseCase: StepUseCase,
  private val recipeUseCase: RecipeUseCase,
  private val userUseCase: UserUseCase
) : ViewModel() {

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
    portion: Int,
    duration: Int,
    image: String,
    steps: List<Step>,
    onResult: (msg: Int?) -> Unit
  ) {
    getUserFromDB(object : FirestoreCallback {
      override fun onSuccess(user: User?) {

        if (NetworkUtils.isConnectedToNetwork.value == false) {
          onResult(R.string.no_internet)
          return
        }

        user?.let { me ->
          try {
            val recipeId = getRecipeDocumentId(me.id)

            db.runTransaction { transaction ->
              val recipe = Recipe(
                id = recipeId,
                title = title,
                description = description,
                portion = portion,
                duration = duration,
                image = image,
                date = Date(),
              )
              val recipeToFirebase = RecipeResponse.transform(recipe)

              // create menu id
              val menuRefs = steps.map { step ->
                db.collection("steps").document(step.id) to step
              }

              val snapshots = menuRefs.map { (ref, _) ->
                transaction.get(ref)
              }

              transaction.set(
                db.collection("recipes").document(recipe.id), recipeToFirebase
              )

              snapshots.forEachIndexed { index, snapshot ->
                val (ref, step) = menuRefs[index]

                val stepResponse =
                  StepResponse.transform(step.copy(id = getRecipeStepDocumentId(recipe.id)))

                transaction.set(
                  db.collection("recipes").document(recipe.id).collection("steps")
                    .document(step.id), stepResponse
                )
//                  transaction.update(ref, "menu_stock", newStock)
              }
            }.addOnSuccessListener {

              Log.d("createRecipeViewModel", "success")
              onResult(null)
            }.addOnFailureListener { e ->
              Timber.tag("createRecipeViewModel").d("transaction failed: %s", e.message)
              onResult(R.string.save_failed)
            }
          } catch (e: Exception) {
            Timber.tag("createRecipeViewModel")
              .d("transaction faled: %s", e.message)
            onResult(R.string.save_failed_2)
          }
        }
      }

      override fun onFailure(exception: Exception) {
        onResult(R.string.save_failed_user)
      }
    })
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

}