package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Reply
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class ReplyResponse(
  @get:PropertyName("reply_id")
  @set:PropertyName("reply_id")
  var id: String = "",

  @get:PropertyName("reply_body")
  @set:PropertyName("reply_body")
  var body: String = "",

  @get:PropertyName("reply_date")
  @set:PropertyName("reply_date")
  var date: Date = Date(),

  @get:PropertyName("replies_id")
  @set:PropertyName("replies_id")
  var repliesId: List<String> = listOf(), //rootReplyId_Id

  @get:PropertyName("reply_user_id")
  @set:PropertyName("reply_user_id")
  var userId: String = "", //rootReplyId_Id

  @get:PropertyName("reply_upvotes")
  @set:PropertyName("reply_upvotes")
  var upvotes: Int = -1
) {

  constructor() : this("")

  companion object {
    fun transform(replyResponse: ReplyResponse): Reply {
      return Reply(
        id = replyResponse.id,
        body = replyResponse.body,
        date = replyResponse.date,
        repliesId = replyResponse.repliesId,
        upvotes = replyResponse.upvotes,
        userId = replyResponse.userId,
      )
    }

    fun transform(reply: Reply): ReplyResponse {
      return ReplyResponse(
        id = reply.id,
        body = reply.body,
        date = reply.date,
        repliesId = reply.repliesId,
        upvotes = reply.upvotes,
        userId = reply.userId
      )
    }
  }
}