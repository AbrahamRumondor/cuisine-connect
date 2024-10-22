package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.model.Step
import com.google.firebase.firestore.PropertyName

data class HashtagResponse(
  @get:PropertyName("hashtag_id")
  @set:PropertyName("hashtag_id")
  var id: String = "",

  @get:PropertyName("hashtag_body")
  @set:PropertyName("hashtag_body")
  var body: String = "",

  @get:PropertyName("hashtag_timestamps")
  @set:PropertyName("hashtag_timestamps")
  var timeStamps: Map<String, Int> = emptyMap(),  // Changed Long to String for Firestore compatibility

  @get:PropertyName("hashtag_total_score")
  @set:PropertyName("hashtag_total_score")
  var totalScore: Int = 0
) {

  constructor() : this("")

  companion object {
    fun transform(hashtagResponse: HashtagResponse): Hashtag {
      return Hashtag(
        id = hashtagResponse.id,
        body = hashtagResponse.body,
        timeStamps = hashtagResponse.timeStamps.mapKeys { it.key.toLong() }, // Convert keys back to Long
        totalScore = hashtagResponse.totalScore
      )
    }

    fun transform(hashtag: Hashtag): HashtagResponse {
      return HashtagResponse(
        id = hashtag.id,
        body = hashtag.body,
        timeStamps = hashtag.timeStamps.mapKeys { it.key.toString() }, // Convert keys to String for Firestore
        totalScore = hashtag.totalScore
      )
    }
  }
}