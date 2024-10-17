package com.example.cuisineconnect.app.screen.profile.post

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemPostViewHolder
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemRecipeViewHolder
import com.example.cuisineconnect.databinding.ItemPostHorizontalBinding
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint

class ProfilePostAdapter :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var createPostViewModel: CreatePostViewModel? = null

  private var items: MutableList<Pair<Any?, Any?>> = mutableListOf()
  private var postNRecipeItemListener: RecipeListListener? = null

  private val SHOW_RECIPE = 0
  private val SHOW_POST = 1

  override fun getItemViewType(position: Int): Int {
    return when (items[position].second) {
      is Post -> SHOW_POST
      is Recipe -> SHOW_RECIPE
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)

    return when (viewType) {
      SHOW_POST -> {
        val bindingPost = ItemPostHorizontalBinding.inflate(inflater, parent, false)
        ItemPostViewHolder(bindingPost)
      }

      SHOW_RECIPE -> {
        val bindingRecipe = ItemRecipeHorizontalBinding.inflate(inflater, parent, false)
        ItemRecipeViewHolder(bindingRecipe)
      }

      else -> throw IllegalArgumentException("Invalid view type")
    }
  }


  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val first = items[position].first
    val second = items[position].second
    when {
      second is Post && first is User? -> (holder as ItemPostViewHolder).bind(
        first,
        second,
        postNRecipeItemListener,
        createPostViewModel
      )

      second is Recipe && first is User? -> (holder as ItemRecipeViewHolder).bind(
        first,
        second,
        postNRecipeItemListener
      )

      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun submitPostNRecipeParts(orderItems: MutableList<Pair<User, Any?>>) {
    Log.d("lililil", "ini list: ${orderItems}")
    val validItems = orderItems.filter { pair ->
      pair.second is Post || pair.second is Recipe
    }

    this.items.clear()
    this.items.addAll(validItems)
    notifyDataSetChanged()
  }

  fun setItemListener(postNRecipeItemListener: RecipeListListener) {
    this.postNRecipeItemListener = postNRecipeItemListener
  }

  fun addViewModel(createPostViewModel: CreatePostViewModel) {
    this.createPostViewModel = createPostViewModel
  }

  fun removeData(itemId: String, type: String) {
    val indexToRemove = items.indexOfFirst {
      when (type) {
        "recipe" -> it.second is Recipe && (it.second as Recipe).id == itemId
        "post" -> it.second is Post && (it.second as Post).id == itemId
        else -> false
      }
    }

    if (indexToRemove != -1) {
      val updatedItems = items.toMutableList()
      updatedItems.removeAt(indexToRemove)

      items = updatedItems
      notifyItemRemoved(indexToRemove)
    }
  }
}