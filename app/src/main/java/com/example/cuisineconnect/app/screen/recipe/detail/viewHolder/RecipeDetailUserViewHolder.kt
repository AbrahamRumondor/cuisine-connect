package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.UserClickListener
import com.example.cuisineconnect.databinding.UserHorizontalBinding
import com.example.cuisineconnect.domain.model.User

class RecipeDetailUserViewHolder(
  private val view: UserHorizontalBinding,
    private val itemListener: UserClickListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(user: User?) {
    view.run {
      if (user != null) {

        tvUsername.text = user.displayName
        val follower = if (user.follower.size == 1) "${user.follower.size} follower" else "${user.follower.size} followers"
        tvFollower.text = follower

        Glide.with(root)
          .load(user.image)
          .placeholder(R.drawable.ic_bnv_profile)
          .into(ivUserProfile)

        root.setOnClickListener {
          itemListener?.onUserClicked(user.id)
        }

      }
    }
  }
}