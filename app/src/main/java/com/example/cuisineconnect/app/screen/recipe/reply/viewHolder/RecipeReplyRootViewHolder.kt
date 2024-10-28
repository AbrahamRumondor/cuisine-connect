package com.example.cuisineconnect.app.screen.recipe.reply.viewHolder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.app.screen.recipe.reply.ReplyRecipeViewModel
import com.example.cuisineconnect.databinding.ReplyRecipeRootBinding
import com.example.cuisineconnect.domain.callbacks.ReplyCountCallback
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User

class RecipeReplyRootViewHolder(
  private val view: ReplyRecipeRootBinding,
  private val itemListener: RecipeReplyItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(position: Int, user: User?, reply: Reply?, viewModel: ReplyRecipeViewModel) {
    view.run {
      if (reply != null && user != null) {
        tvUsername.text = user.name
        Glide.with(root)
          .load(user.image)
          .placeholder(android.R.drawable.ic_menu_report_image)
          .into(ivUserProfile)

        llUser.setOnClickListener { itemListener?.onProfilePictureClicked(user.id) }

        // Contents
        tvBody.text = reply.body
        tvReplyUpvote.text = reply.upvotes.toString()

        upvoteLogic(reply, user)
        llUpvote.setOnClickListener {
          if (upvoteLogic(reply, user)) {
            itemListener?.onDownVoteClicked(position, reply, user.id)
          } else {
            itemListener?.onUpvoteClicked(position, reply, user.id)
          }
          upvoteLogic(reply, user)
        }

        // Replies
        tvBtnReply.setOnClickListener {
          itemListener?.onReplyInputClicked(position, reply.id, user)
        }

        if (reply.repliesId.isEmpty()) {
          llShowReply.visibility = View.INVISIBLE
        } else {
          llShowReply.visibility = View.VISIBLE

          val replyCountCallback = object : ReplyCountCallback {
            override fun onReplyCountRetrieved(count: Int) {
              // Only update the reply count if it's not currently showing "Hide replies"
              if (tvShowReply.text != "Hide replies") {
                val repliesText = "view $count replies"
                tvShowReply.text = repliesText
              }
            }

            override fun onError(e: Exception) {
              if (tvShowReply.text != "Hide replies") {
                tvShowReply.text = "Error loading replies"
              }
            }
          }

// Fetch total reply count with callback
          viewModel.fetchTotalReplyCount(
            getItemIdFromReplyId(reply.id),
            reply.id,
            replyCountCallback
          )

          llShowReply.setOnClickListener {
            if (tvShowReply.text == "Hide replies") {
              // Change text back to the number of replies when hiding
              tvShowReply.text = "view ${reply.repliesId.size} replies"
              itemListener?.onReplyListSecondClicked(position, reply.id, reply.repliesId)
            } else {
              // Change text to "Hide replies" when showing the replies
              tvShowReply.text = "Hide replies"
              itemListener?.onReplyListClicked(position, reply.id, reply.repliesId)
            }
          }
        }
      }
    }
  }

  private fun upvoteLogic(reply: Reply, user: User): Boolean {
    val upVoted = reply.upvotes[user.id] ?: false
    view.run {
      ivUpvote.setImageResource(
        if (upVoted) R.drawable.ic_thumbs_up_solid else R.drawable.ic_thumbs_up_regular
      )
      tvReplyUpvote.text = reply.upvotes.size.toString()
    }
    return upVoted
  }

  private fun getItemIdFromReplyId(replyId: String): String {
    return replyId.split("_").take(3).joinToString("_").ifEmpty { replyId }
  }
}