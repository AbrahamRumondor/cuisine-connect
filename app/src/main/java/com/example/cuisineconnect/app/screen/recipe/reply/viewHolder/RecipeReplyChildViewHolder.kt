package com.example.cuisineconnect.app.screen.recipe.reply.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.databinding.ReplyRecipeChildBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User

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
}