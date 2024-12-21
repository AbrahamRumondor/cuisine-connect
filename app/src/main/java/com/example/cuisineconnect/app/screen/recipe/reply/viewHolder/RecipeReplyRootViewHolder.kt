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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecipeReplyRootViewHolder(
  private val view: ReplyRecipeRootBinding,
  private val itemListener: RecipeReplyItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(position: Int, user: User?, reply: Reply?, viewModel: ReplyRecipeViewModel) {
    view.run {
      if (reply != null && user != null) {
        tvUsername.text = user.displayName
        Glide.with(root)
          .load(user.image)
          .placeholder(R.drawable.ic_bnv_profile)
          .into(ivUserProfile)

        llUser.setOnClickListener { itemListener?.onProfilePictureClicked(user.id) }

        // Contents
        tvBody.text = reply.body
        tvReplyUpvote.text = reply.upvotes.toString()
        val formattedDate = getRelativeTime(reply.date) // recipe.date is of type Date
        tvUploadDate.text = formattedDate

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
              if (tvShowReply.text != view.root.context.getString(R.string.hide_replies)) {
                val repliesText = view.root.context.getString(R.string.view_replies, count)
                tvShowReply.text = repliesText
              }
            }

            override fun onError(e: Exception) {
              if (tvShowReply.text != view.root.context.getString(R.string.hide_replies)) {
                tvShowReply.text = view.root.context.getString(R.string.error_loading_replies)
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
            if (tvShowReply.text == view.root.context.getString(R.string.hide_replies)) {
              // Change text back to the number of replies when hiding
              tvShowReply.text = view.root.context.getString(R.string.view_replies, reply.repliesId.size)
              itemListener?.onReplyListSecondClicked(position, reply.id, reply.repliesId)
            } else {
              // Change text to "Hide replies" when showing the replies
              tvShowReply.text = view.root.context.getString(R.string.hide_replies)
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

  private fun getRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val timestamp = date.time
    val diff = now - timestamp

    return when {
      // Less than 1 minute ago
      diff < TimeUnit.MINUTES.toMillis(1) -> {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds <= 1) "$seconds ${view.root.context.getString(R.string.second_ago)}" else "$seconds ${view.root.context.getString(R.string.seconds_ago)}"
      }

      // Less than 1 hour ago
      diff < TimeUnit.HOURS.toMillis(1) -> {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes == 1L) "$minutes ${view.root.context.getString(R.string.minute_ago)}" else "$minutes ${view.root.context.getString(R.string.minutes_ago)}"
      }

      // Less than 1 day ago
      diff < TimeUnit.HOURS.toMillis(24) -> {
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours == 1L) "$hours ${view.root.context.getString(R.string.hour_ago)}" else "$hours ${view.root.context.getString(R.string.hours_ago)}"
      }

      // Less than 5 days ago
      diff < TimeUnit.DAYS.toMillis(5) -> {
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days == 1L) "$days ${view.root.context.getString(R.string.day_ago)}" else "$days ${view.root.context.getString(R.string.days_ago)}"
      }

      // Default to the "MMM dd" format for anything older
      else -> {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateFormat.format(date)
      }
    }
  }

}