package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.repository.ReplyRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class ReplyRepositoryImpl @Inject constructor(
  @Named("recipesRef") private val recipesRef: CollectionReference,
) : ReplyRepository {

  private val _replies = MutableStateFlow<List<Reply>>(emptyList())
  private val replies: StateFlow<List<Reply>> = _replies

  override suspend fun getRepliesByRecipe(recipeId: String): StateFlow<List<Reply>> {
    return try {
      val snapshot = recipesRef.document(recipeId)
        .collection("replies")
        .get()
        .await()

      val repliesList = snapshot.documents.mapNotNull { document ->
        document.toObject(ReplyResponse::class.java)?.let { ReplyResponse.transform(it) }
      }

      _replies.value = repliesList

      replies
    } catch (e: Exception) {
      Timber.e(e, "Error fetching replies for recipe: $recipeId")
      _replies.value = emptyList()

      replies
    }
  }

  override fun getRecipeReplyDocID(recipeId: String): String {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    return "${recipeId}_${repliesRef.document(recipeId).collection("replies").document().id}"
  }

  override fun getChildReplyDocID(recipeId: String, rootReplyId: String): String {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    return "${rootReplyId}_${repliesRef.document(rootReplyId).collection("replies").document().id}"
  }

  override fun setReply(recipeId: String, replyId: String, replyResponse: ReplyResponse) {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    repliesRef.document(replyId).set(replyResponse).addOnSuccessListener {
      Timber.tag("TEST").d("SUCCESS ON reply INSERTION, ${replyResponse}")
    }
      .addOnFailureListener { Timber.tag("TEST").d("ERROR ON reply INSERTION") }
  }

  override suspend fun getReplyById(recipeId: String, replyId: String): Reply? {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    return try {
      val snapshot = repliesRef.document(replyId).get().await()
      val reply =
        snapshot.toObject(ReplyResponse::class.java)?.let { ReplyResponse.transform(it) }

      reply
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun upvoteReply(recipeId: String, replyId: String) {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    try {
      val recipeDoc = repliesRef.document(replyId)
      val snapshot = recipeDoc.get().await()
      val replyResponse = snapshot.toObject(ReplyResponse::class.java)

      replyResponse?.let { response ->
        val upvotes = response.upvotes + 1

        response.upvotes = upvotes
        recipeDoc.set(response).await()
        Timber.tag("Upvote").d("User upvoted reply successfully")
      }
    } catch (e: Exception) {
      Timber.tag("Upvote").e(e, "Error upvoting reply")
    }
  }

  // New method to remove an upvote
  override suspend fun removeUpvote(recipeId: String, replyId: String) {
    val repliesRef = recipesRef.document(recipeId).collection("replies")

    try {
      val recipeDoc = repliesRef.document(replyId)
      val snapshot = recipeDoc.get().await()
      val replyResponse = snapshot.toObject(ReplyResponse::class.java)

      replyResponse?.let { response ->
        if (response.upvotes > 0) {
          val upvotes = response.upvotes - 1

          response.upvotes = upvotes
          recipeDoc.set(response).await()
          Timber.tag("RemoveUpvote")
            .d("User removed upvote from reply")
        }
      }
    } catch (e: Exception) {
      Timber.tag("RemoveUpvote").e(e, "Error removing upvote from reply")
    }
  }

}