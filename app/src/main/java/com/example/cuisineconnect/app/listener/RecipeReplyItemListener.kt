package com.example.cuisineconnect.app.listener

import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User

interface RecipeReplyItemListener {
  fun onProfilePictureClicked(userId: String)
  fun onUpvoteClicked(position: Int, reply: Reply, userId: String)
  fun onDownVoteClicked(position: Int, reply: Reply, userId: String)
  fun onReplyInputClicked(position: Int, targetReplyId: String, user: User)
  fun onReplyListClicked(position: Int, replyId: String, repliesId: List<String>)
  fun onReplyListSecondClicked(position: Int, replyId: String, repliesId: List<String>)
  // uses {} for default implementation (so dont have to impelemnts).
}