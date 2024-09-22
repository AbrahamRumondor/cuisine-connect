package com.example.cuisineconnect.app.listener

interface RecipeReplyItemListener {
  fun onProfilePictureClicked(userId: String)
  fun onUpvoteClicked(position: Int, replyId: String)
  fun onReplyInputClicked(position: Int, targetReplyId: String)
  fun onReplyListClicked(position: Int, replyId: String, repliesId: List<String>)
  // uses {} for default implementation (so dont have to impelemnts).
}