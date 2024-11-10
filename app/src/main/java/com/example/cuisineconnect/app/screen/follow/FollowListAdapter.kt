package com.example.cuisineconnect.app.screen.follow

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.ItemUserListBinding
import com.example.cuisineconnect.domain.model.User

class FollowListAdapter : RecyclerView.Adapter<FollowListAdapter.ViewHolder>() {

  private var listener: FollowListListener? = null
  private var users: List<User> = listOf()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = ItemUserListBinding.inflate(
      LayoutInflater.from(parent.context), parent, false
    )
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val user = users[position]
    holder.bind(user)
  }

  override fun getItemCount(): Int = users.size

  fun submitUsers(newUsers: List<User>) {
    users = newUsers
    notifyDataSetChanged()
  }

  fun setItemListener(followListListener: FollowListListener) {
    this.listener = followListListener
  }

  inner class ViewHolder(private val binding: ItemUserListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
      binding.tvUsername.text = user.name
      binding.tvUniqueUsername.text = "@${user.name}"

      Glide.with(binding.ivUserProfile.context)
        .load(user.image)
        .placeholder(R.drawable.ic_bnv_profile)
        .circleCrop()
        .into(binding.ivUserProfile)

      binding.root.setOnClickListener {
        listener?.onUserClicked(user.id)
        Toast.makeText(binding.root.context, "Clicked on ${user.name}", Toast.LENGTH_SHORT).show()
      }
    }
  }
}

interface FollowListListener {
  fun onUserClicked(userId: String)
}