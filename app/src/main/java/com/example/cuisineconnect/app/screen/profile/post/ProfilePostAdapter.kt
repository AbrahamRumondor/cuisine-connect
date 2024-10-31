package com.example.cuisineconnect.app.screen.profile.post

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.post.viewHolder.ItemPostViewHolder
import com.example.cuisineconnect.databinding.ItemPostHorizontalBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User

class ProfilePostAdapter :
  RecyclerView.Adapter<ItemPostViewHolder>() {

  private var createPostViewModel: CreatePostViewModel? = null

  private var items: MutableList<Pair<User, Post?>> = mutableListOf()
  private var postItemListener: ItemListListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPostViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val bindingPost = ItemPostHorizontalBinding.inflate(inflater, parent, false)
    return ItemPostViewHolder(bindingPost)
  }

  override fun onBindViewHolder(holder: ItemPostViewHolder, position: Int) {
    val (user, post) = items[position]
    holder.bind(user, post?:Post(), postItemListener, createPostViewModel)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun submitPosts(postItems: MutableList<Pair<User, Post?>>) {
    Log.d("ProfilePostAdapter", "Post list: $postItems")
    this.items.clear()
    this.items.addAll(postItems)
    notifyDataSetChanged()
  }

  fun setItemListener(postItemListener: ItemListListener) {
    this.postItemListener = postItemListener
  }

  fun addViewModel(createPostViewModel: CreatePostViewModel) {
    this.createPostViewModel = createPostViewModel
  }

  fun removeData(postId: String) {
    val indexToRemove = items.indexOfFirst { it.second?.id == postId }

    if (indexToRemove != -1) {
      items.removeAt(indexToRemove)
      notifyItemRemoved(indexToRemove)
    }
  }
}