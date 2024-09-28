package com.example.cuisineconnect.domain.model

import java.util.Date

data class Reply(
  var id: String = "",
  var body: String = "",
  var date: Date = Date(),
  var repliesId: List<String> = listOf(), //rootReplyId_Id
  var upvotes: Int = -1,
  var userId: String = "",
  var parentId: String = "",

  var isRoot: Int = 0
)