package com.example.cuisineconnect.data.repository

import com.example.cuisineconnect.app.listener.TrendingHashtagsCallback
import com.example.cuisineconnect.data.response.HashtagResponse
import com.example.cuisineconnect.domain.repository.HashtagRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Named

class HashtagRepositoryImpl @Inject constructor(
  @Named("hashtagsRef") private val hashtagRef: CollectionReference // Changed from `usersRef` to `hashtagsRef`
) : HashtagRepository {

  override fun updateHashtagWithScore(hashtagBody: String, newTimestamp: Long, increment: Int) {
    hashtagRef.whereEqualTo("hashtag_body", hashtagBody)
      .get()
      .addOnSuccessListener { snapshot ->
        if (!snapshot.isEmpty) {
          // Assuming hashtags are unique by body, get the first document
          val document = snapshot.documents.first()
          val hashtag = document.toObject(HashtagResponse::class.java)
          val currentTimestamps = hashtag?.timeStamps?.toMutableMap() ?: mutableMapOf()

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

          // Calculate the new total score
          val newTotalScore = currentTimestamps.values.sum()

          // Update Firestore with the new timestamps and total score
          hashtagRef.document(document.id).update(
            mapOf(
              "hashtag_timestamps" to currentTimestamps, // Use string-keyed timestamps
              "hashtag_total_score" to newTotalScore
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

  override fun addHashtag(hashtagBody: String, callback: (Boolean, Exception?) -> Unit) {
    // Create a new HashtagResponse object
    val newHashtag = HashtagResponse(
      body = hashtagBody,
      timeStamps = emptyMap(),
      totalScore = 0
    )

    // Add the new hashtag to Firestore
    hashtagRef.add(newHashtag)
      .addOnSuccessListener { documentReference ->
        println("Hashtag added with ID: ${documentReference.id}")
        callback(true, null) // Successfully added
      }
      .addOnFailureListener { e ->
        println("Error adding hashtag: ${e.message}")
        callback(false, e) // Handle the error
      }
  }
}