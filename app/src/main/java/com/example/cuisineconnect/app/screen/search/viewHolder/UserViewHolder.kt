package com.example.cuisineconnect.app.screen.search.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.databinding.ItemUserHorizontalBinding
import com.example.cuisineconnect.domain.model.User

class UserViewHolder(
    private val view: ItemUserHorizontalBinding,
    private val itemListener: OnClickItemListener?
) : RecyclerView.ViewHolder(view.root) {

    fun bind(user: User?) {
        view.run {
            if (user != null) {
                Glide.with(root)
                    .load(user.image)
                    .placeholder(R.drawable.ic_bnv_profile)
                    .into(ivUserProfile)

                tvUsername.text = user.displayName
                val username = "@${user.username}"
                tvUniqueUsername.text = username

                root.setOnClickListener {
                    itemListener?.onUserClicked(user)
                }
            }
        }
    }
}