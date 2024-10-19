package com.example.cuisineconnect.app.screen.search.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(ivUserProfile)

                tvUsername.text = user.name
                val uniqueName = "@${user.name}"
                tvUniqueUsername.text = uniqueName

                root.setOnClickListener {
                    itemListener?.onUserClicked(user)
                }
            }
        }
    }
}