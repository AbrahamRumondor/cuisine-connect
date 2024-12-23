package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback
import com.example.cuisineconnect.data.response.HashtagResponse
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.repository.HashtagRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class HashtagRepositoryImpl @Inject constructor(
  @Named("hashtagsRef") private val hashtagRef: CollectionReference // Changed from `usersRef` to `hashtagsRef`
) : HashtagRepository {

  override fun updateHashtagWithScore(
    hashtagBody: String,
    itemId: String,
    newTimestamp: Long,
    increment: Int
  ) {
    hashtagRef.whereEqualTo("hashtag_body", hashtagBody)
      .get()
      .addOnSuccessListener { snapshot ->
        if (!snapshot.isEmpty) {
          // Assuming hashtags are unique by body, get the first document
          val document = snapshot.documents.first()
          val hashtag = document.toObject(HashtagResponse::class.java)
          val currentTimestamps = hashtag?.timeStamps?.toMutableMap() ?: mutableMapOf()
          val currentListId = hashtag?.listId?.toMutableList() ?: mutableListOf()

          // Remove timestamps older than 24 hours
          val currentTime = System.currentTimeMillis()
          currentTimestamps.entries.removeIf { entry ->
            // Check if the entry's timestamp is older than 24 hours (in milliseconds)
            val isOlderThan24Hours = (currentTime - entry.key.toLong()) > (24 * 60 * 60 * 1000)
            isOlderThan24Hours
          }

          // Convert newTimestamp to String for Firestore
          currentTimestamps[newTimestamp.toString()] =
            currentTimestamps.getOrDefault(newTimestamp.toString(), 0) + increment

          // Ensure itemId is added only once
          if (!currentListId.contains(itemId)) {
            currentListId.add(itemId)
          }

          // Calculate the new total score
          val newTotalScore = currentTimestamps.values.sum()

          // Update Firestore with the new timestamps, total score, and updated listId
          hashtagRef.document(document.id).update(
            mapOf(
              "hashtag_timestamps" to currentTimestamps, // Use string-keyed timestamps
              "hashtag_total_score" to newTotalScore,
              "hashtag_list_id" to currentListId // Update the listId with the new item
            )
          ).addOnSuccessListener {
            println("Hashtag updated successfully.")
          }.addOnFailureListener { e ->
            println("Error updating hashtag: ${e.message}")
          }
        } else {
          println("No hashtag found with the body: $hashtagBody.")
        }
      }.addOnFailureListener { e ->
        println("Error fetching hashtags: ${e.message}")
      }
  }

  override fun getTrendingHashtags(callback: TrendingHashtagsCallback) {
    hashtagRef.orderBy("hashtag_total_score", Query.Direction.DESCENDING)
      .limit(10) // Limit to the top 10 hashtags
      .get()
      .addOnSuccessListener { snapshot ->
        val trendingHashtags = snapshot.documents.mapNotNull { document ->
          document.toObject(HashtagResponse::class.java)?.let {
            HashtagResponse.transform(it)
          }
        }

        callback.onTrendingHashtagsReceived(trendingHashtags)
      }
      .addOnFailureListener { e ->
        println("Error fetching trending hashtags: ${e.message}")
        callback.onError(e)
      }
  }

  override fun addHashtag(
    hashtagBody: String,
    itemId: String,
    newTimestamp: Long,
    increment: Int,
    callback: (Boolean, Exception?) -> Unit
  ) {
    // Check if the hashtag already exists
    hashtagRef.whereEqualTo("hashtag_body", hashtagBody)
      .get()
      .addOnSuccessListener { snapshot ->
        if (!snapshot.isEmpty) {
          // If the hashtag exists, get the document ID
          val document = snapshot.documents.first()
          // Attempt to update the hashtag with the new score
          updateHashtagWithScore(hashtagBody, itemId, newTimestamp, increment)
          callback(true, null) // Indicate that the hashtag was updated
        } else {
          // If the hashtag does not exist, create a new one
          val newHashtag = HashtagResponse(
            id = hashtagRef.document().id, // Firestore will generate an ID if you use .add()
            body = hashtagBody,
            timeStamps = emptyMap(), // Use empty map for timeStamps if it's intended to be updated later
            totalScore = 0
          )

          // Add the new hashtag to Firestore
          hashtagRef.add(newHashtag)
            .addOnSuccessListener { documentReference ->
              // After adding, update the hashtag with the score
              updateHashtagWithScore(hashtagBody, itemId, newTimestamp, increment)
              callback(true, null) // Successfully added and updated
            }
            .addOnFailureListener { e ->
              println("Error adding hashtag: ${e.message}")
              callback(false, e) // Handle the error
            }
        }
      }
      .addOnFailureListener { e ->
        println("Error checking for existing hashtag: ${e.message}")
        callback(false, e) // Handle the error
      }
  }

  override fun searchHashtags(query: String, callback: (List<Hashtag>, Exception?) -> Unit) {
    hashtagRef
      .orderBy("hashtag_body")
      .startAt(query)
      .endAt(query + "\uf8ff")
      .get()
      .addOnSuccessListener { snapshot ->
        val hashtagList = snapshot.documents.mapNotNull { document ->
          document.toObject(HashtagResponse::class.java)?.let { HashtagResponse.transform(it) }
        }

        // Add a special item for creating a new hashtag
        if (hashtagList.isEmpty()) {
          val createNewHashtag = Hashtag(
            id = "create_new", // Special ID
            body = "Create new hashtag"
          )
          callback(hashtagList + createNewHashtag, null)
        } else {
          callback(hashtagList, null)
        }
      }
      .addOnFailureListener { e ->
        callback(emptyList(), e)
      }
  }

  override fun findSearchPromptHashtags(
    query: String,
    callback: (List<Hashtag>, Exception?) -> Unit
  ) {
    hashtagRef
      .orderBy("hashtag_body")
      .startAt(query)
      .endAt(query + "\uf8ff")
      .get()
      .addOnSuccessListener { snapshot ->
        val hashtagList = snapshot.documents.mapNotNull { document ->
          document.toObject(HashtagResponse::class.java)?.let { HashtagResponse.transform(it) }
        }
        callback(hashtagList, null)
      }
      .addOnFailureListener { e ->
        callback(emptyList(), e)
      }
  }

  fun updateTrendingScores(callback: (Boolean, Exception?) -> Unit) {
    hashtagRef.get()
      .addOnSuccessListener { snapshot ->
        val currentTime = System.currentTimeMillis()
        snapshot.documents.forEach { document ->
          val hashtag = document.toObject(HashtagResponse::class.java)
          val currentTimestamps = hashtag?.timeStamps?.toMutableMap() ?: mutableMapOf()

          // Remove timestamps older than 24 hours
          currentTimestamps.entries.removeIf { entry ->
            (currentTime - entry.key.toLong()) > (24 * 60 * 60 * 1000)
          }

          // Recalculate total score
          val newTotalScore = currentTimestamps.values.sum()

          // Update Firestore with new timestamps and total score if changed
          if (newTotalScore != hashtag?.totalScore) {
            hashtagRef.document(document.id).update(
              mapOf(
                "hashtag_timestamps" to currentTimestamps,
                "hashtag_total_score" to newTotalScore
              )
            ).addOnFailureListener { e ->
              println("Error updating hashtag scores: ${e.message}")
              callback(false, e)
            }
          }
        }
        callback(true, null) // All hashtags updated successfully
      }
      .addOnFailureListener { e ->
        println("Error fetching hashtags for updating scores: ${e.message}")
        callback(false, e)
      }
  }

  override fun getSortedTrendingHashtags(
    limit: Int,
    callback: (List<Hashtag>, Exception?) -> Unit
  ) {
    updateTrendingScores { success, error ->
      if (success) {
        // Proceed to fetch trending hashtags if scores were successfully updated
        hashtagRef
          .orderBy("hashtag_total_score", Query.Direction.DESCENDING)
          .limit(limit.toLong())
          .get()
          .addOnSuccessListener { snapshot ->
            val trendingHashtags = snapshot.documents.mapNotNull { document ->
              document.toObject(HashtagResponse::class.java)?.let { HashtagResponse.transform(it) }
            }
            callback(trendingHashtags, null)
          }
          .addOnFailureListener { e ->
            callback(emptyList(), e)
          }
      } else {
        // Pass the error if updating scores failed
        callback(emptyList(), error)
      }
    }
  }

  override suspend fun removeItemIdFromHashtag(hashtagBody: String, itemId: String) {
    try {
      val snapshot = hashtagRef.whereEqualTo("hashtag_body", hashtagBody).get().await()
      if (snapshot.isEmpty) {
        Timber.tag("RemoveHashtag").d("No hashtag found with the body: $hashtagBody")
        return
      }

      val document = snapshot.documents.first()
      val hashtag = document.toObject(HashtagResponse::class.java)
      val currentListId = hashtag?.listId?.toMutableList() ?: mutableListOf()

      if (currentListId.contains(itemId)) {
        currentListId.remove(itemId)

        if (currentListId.isEmpty()) {
          hashtagRef.document(document.id).delete().await()
          Timber.tag("RemoveHashtag").d("Hashtag $hashtagBody removed successfully.")
        } else {
          hashtagRef.document(document.id).update("hashtag_list_id", currentListId).await()
          Timber.tag("RemoveHashtag").d("Updated hashtag $hashtagBody successfully.")
        }
      } else {
        Timber.tag("RemoveHashtag").d("Item ID $itemId not found in hashtag $hashtagBody.")
      }
    } catch (e: Exception) {
      Timber.tag("RemoveHashtag").e(e, "Error removing itemId $itemId from hashtag $hashtagBody")
    }
  }
}