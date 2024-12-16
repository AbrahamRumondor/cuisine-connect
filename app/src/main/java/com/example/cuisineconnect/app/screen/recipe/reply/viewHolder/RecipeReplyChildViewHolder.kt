package com.example.cuisineconnect.app.screen.recipe.reply.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.databinding.ReplyRecipeChildBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecipeReplyChildViewHolder(
    private val view: ReplyRecipeChildBinding,
    private val itemListener: RecipeReplyItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(position: Int, user: User?, reply: Reply?, userTarget: User?) {
        view.run {
            if (reply != null && user != null) {

                tvUsername.text = user.displayName

                Glide.with(root)
                    .load(user.image)
                    .placeholder(R.drawable.ic_bnv_profile)
                    .into(ivUserProfile)

                llUser.setOnClickListener {
                    itemListener?.onProfilePictureClicked(user.id)
                }

                // contents
                val text = "@${userTarget?.displayName ?: "User"} " + reply.body
                tvBody.text = text
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

                // replies
                tvBtnReply.setOnClickListener {
                    itemListener?.onReplyInputClicked(position, reply.id, user)
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

    private fun getRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val timestamp = date.time
        val diff = now - timestamp

        return when {
            // Less than 1 minute ago
            diff < TimeUnit.MINUTES.toMillis(1) -> {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
                if (seconds <= 1) "$seconds second ago" else "$seconds seconds ago"
            }

            // Less than 1 hour ago
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                if (minutes == 1L) "$minutes minute ago" else "$minutes minutes ago"
            }

            // Less than 1 day ago
            diff < TimeUnit.HOURS.toMillis(24) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                if (hours == 1L) "$hours hour ago" else "$hours hours ago"
            }

            // Less than 5 days ago
            diff < TimeUnit.DAYS.toMillis(5) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                if (days == 1L) "$days day ago" else "$days days ago"
            }

            // Default to the "MMM dd" format for anything older
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    }
}