package com.example.cuisineconnect.app.screen.recipe.reply.viewHolder

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.databinding.RecipeDetailStepBinding
import com.example.cuisineconnect.databinding.ReplyRecipeChildBinding
import com.example.cuisineconnect.databinding.ReplyRecipeRootBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User

class RecipeReplyChildViewHolder(
    private val view: ReplyRecipeChildBinding,
    private val itemListener: RecipeReplyItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(position: Int, user: User?, reply: Reply?, userTarget: User?) {
        view.run {
            if (reply != null && user != null) {

                tvUsername.text = user.name

                Glide.with(root)
                    .load(user.image)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(ivUserProfile)

                llUser.setOnClickListener {
                    itemListener?.onProfilePictureClicked(user.id)
                }

                // contents
                val text = "@${userTarget?.name ?: "User"} " + reply.body
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