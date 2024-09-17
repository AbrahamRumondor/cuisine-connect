package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.databinding.UserHorizontalBinding
import com.example.cuisineconnect.domain.model.User

class RecipeDetailUserViewHolder(
  private val view: UserHorizontalBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(user: User?) {
    view.run {
      if (user != null) {

        tvUsername.text = user.name
        tvFollower.text = user.follower.size.toString()

        Glide.with(root)
          .load(user.image)
          .placeholder(android.R.drawable.ic_menu_report_image)
          .into(ivUserProfile)

//                ivOrderAdd.setOnClickListener {
//                    itemListener?.onAddItemClicked(position, menu.id)
//                }
      }
    }
  }
}