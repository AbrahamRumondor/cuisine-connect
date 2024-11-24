package com.example.cuisineconnect.data.repository

import android.util.Log
import androidx.paging.LOG_TAG
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.data.response.ReplyResponse
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.repository.ReplyRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class ReplyRepositoryImpl @Inject constructor(
  @Named("recipesRef") private val recipesRef: CollectionReference,
  @Named("postsRef") private val postsRef: CollectionReference,
) : ReplyRepository {

  private val _replies = MutableStateFlow<List<Reply>>(emptyList())
  private val replies: StateFlow<List<Reply>> = _replies

  // Determine the correct reference based on ID suffix
  private fun getReference(id: String): CollectionReference {
    return if (id.startsWith("r_")) recipesRef else postsRef
  }

  override suspend fun getRepliesByRecipeOrPost(id: String): StateFlow<List<Reply>> {
    return try {
      val ref = getReference(id)
      val snapshot = ref.document(id)
        .collection("replies")
        .get()
        .await()

      val repliesList = snapshot.documents.mapNotNull { document ->
        document.toObject(ReplyResponse::class.java)?.let { ReplyResponse.transform(it) }
      }

      _replies.value = repliesList
      replies
    } catch (e: Exception) {
      Timber.e(e, "Error fetching replies for ID: $id")
      _replies.value = emptyList()
      replies
    }
  }

  override fun getReplyDocID(id: String): String {
    val repliesRef = getReference(id).document(id).collection("replies")
    return "${id}_${repliesRef.document().id}"
  }

  override fun getChildReplyDocID(id: String, rootReplyId: String): String {
    val repliesRef = getReference(id).document(id).collection("replies")
    return "${rootReplyId}_${repliesRef.document().id}"
  }

  override fun setReply(id: String, replyId: String, replyResponse: ReplyResponse, isNewReply: Boolean) {
    val ref = getReference(id)
    val docRef = ref.document(id)
    val repliesRef = docRef.collection("replies")

    // Parse the replyId to get the feedsType and feedId
    val replyParts = replyId.split("_")
    if (replyParts.size < 4) {
      Timber.tag("TEST").e("Invalid replyId format: $replyId")
      return
    }

    val feedsType = replyParts[0] // 'r' for recipe, 'p' for post
    val feedId = replyParts.subList(0, 3).joinToString("_") // Combine the first three parts to reconstruct the feedId

    repliesRef.document(replyId).set(replyResponse)
      .addOnSuccessListener {
        Timber.tag("TEST").d("SUCCESS ON reply INSERTION, $replyResponse")

        if (isNewReply) {
          // Determine the field to update
          val replyCountField = if (feedsType == "r") {
            "recipe_reply_count"
          } else if (feedsType == "p") {
            "post_reply_count"
          } else {
            Timber.tag("TEST").e("Invalid feedsType: $feedsType")
            return@addOnSuccessListener
          }

          // Determine the correct reference to update
          val updateRef = if (feedsType == "r") {
            recipesRef.document(feedId) // Recipe reply count
          } else {
            postsRef.document(feedId) // Post reply count
          }

          // Update the reply count
          updateRef.update(replyCountField, FieldValue.increment(1))
            .addOnSuccessListener {
              Timber.tag("TEST").d("Successfully incremented reply count for $feedId")
            }
            .addOnFailureListener { e ->
              Timber.tag("TEST").e("Failed to increment reply count for $feedId: ${e.message}")
            }
        }
      }
      .addOnFailureListener { e ->
        Timber.tag("TEST").e("ERROR ON reply INSERTION: ${e.message}")
      }
  }

  override suspend fun getReplyById(id: String, replyId: String): Reply? {
    val ref = getReference(id)
    val repliesRef = ref.document(id).collection("replies")

    return try {
      val snapshot = repliesRef.document(replyId).get().await()
      snapshot.toObject(ReplyResponse::class.java)?.let { ReplyResponse.transform(it) }
    } catch (e: Exception) {
      Timber.e(e, "Error fetching reply by ID: $replyId for ID: $id")
      null
    }
  }

  override suspend fun upvoteReply(id: String, repliedId: String, userId: String, result: (Reply) -> Unit) {
    val ref = getReference(id)
    val repliesRef = ref.document(id).collection("replies")

    try {
      val replyDoc = repliesRef.document(repliedId)
      val snapshot = replyDoc.get().await()
      val replyResponse = snapshot.toObject(ReplyResponse::class.java)

      replyResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()
        upvotes[userId] = true
        response.upvotes = upvotes

        replyDoc.set(response).await()
        Timber.tag("Upvote").d("User $userId upvoted reply $repliedId successfully")
        result(ReplyResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("Upvote").e(e, "Error upvoting reply $repliedId for ID: $id by user $userId")
    }
  }

  override suspend fun removeUpvote(id: String, repliedId: String, userId: String, result: (Reply) -> Unit) {
    val ref = getReference(id)
    val repliesRef = ref.document(id).collection("replies")

    try {
      val replyDoc = repliesRef.document(repliedId)
      val snapshot = replyDoc.get().await()
      val replyResponse = snapshot.toObject(ReplyResponse::class.java)

      replyResponse?.let { response ->
        val upvotes = response.upvotes.toMutableMap()
        upvotes.remove(userId)
        response.upvotes = upvotes

        replyDoc.set(response).await()
        Timber.tag("RemoveUpvote").d("User $userId removed upvote from reply $repliedId successfully")
        result(ReplyResponse.transform(response))
      }
    } catch (e: Exception) {
      Timber.tag("RemoveUpvote").e(e, "Error removing upvote from reply $repliedId for ID: $id by user $userId")
    }
  }

  override suspend fun getTotalReplyCount(id: String, replyId: String): Int {
    val ref = getReference(id)
    val repliesRef = ref.document(id).collection("replies")

    var totalCount = 0
    val currentLevelReplies = repliesRef
      .whereEqualTo("reply_parent_id", replyId)
      .get()
      .await()
      .toObjects(ReplyResponse::class.java)

    totalCount += currentLevelReplies.size
    for (reply in currentLevelReplies) {
      Log.d("replyRepoImpl", "reply: $reply")

      totalCount += getTotalReplyCount(id, reply.id)
    }

    Log.d("replyRepoImpl", "ini: $totalCount")
    return totalCount
  }
}