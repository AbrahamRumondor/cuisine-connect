package com.example.cuisineconnect.app.screen.recipe.reply.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    fun bind(position: Int, user: User?, reply: Reply?) {
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
                tvBody.text = reply.body
                tvReplyUpvote.text = reply.upvotes.toString()
                llUpvote.setOnClickListener {
                    itemListener?.onUpvoteClicked(position, reply.id)
                }

                // replies
                tvBtnReply.setOnClickListener {
                    itemListener?.onReplyInputClicked(position, reply.id)
                }
            }
        }
    }
}