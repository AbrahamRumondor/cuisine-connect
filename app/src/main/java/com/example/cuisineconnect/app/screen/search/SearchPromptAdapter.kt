package com.example.cuisineconnect.app.screen.search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemPostViewHolder
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemRecipeViewHolder
import com.example.cuisineconnect.app.screen.search.viewHolder.DividerViewHolder
import com.example.cuisineconnect.app.screen.search.viewHolder.TextViewViewHolder
import com.example.cuisineconnect.app.screen.search.viewHolder.UserViewHolder
import com.example.cuisineconnect.databinding.ItemDividerHorizontalBinding
import com.example.cuisineconnect.databinding.ItemPostHorizontalBinding
import com.example.cuisineconnect.databinding.ItemPostTextViewBinding
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.databinding.ItemSearchTextViewBinding
import com.example.cuisineconnect.databinding.ItemUserHorizontalBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User

class SearchPromptAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var items: MutableList<Any?> = mutableListOf()
  private var onClickItemListener: OnClickItemListener? = null

  private val SHOW_PROMPT = 0
  private val SHOW_DIVIDER = 1
  private val SHOW_USER = 2

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is String -> SHOW_PROMPT
      is Int -> SHOW_DIVIDER
      is User -> SHOW_USER
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)

    return when (viewType) {
      SHOW_PROMPT -> {
        val bindingPrompt = ItemSearchTextViewBinding.inflate(inflater, parent, false)
        TextViewViewHolder(bindingPrompt, onClickItemListener)
      }

      SHOW_DIVIDER -> {
        val bindingDivider = ItemDividerHorizontalBinding.inflate(inflater, parent, false)
        DividerViewHolder(bindingDivider)
      }

      SHOW_USER -> {
        val bindingUser = ItemUserHorizontalBinding.inflate(inflater, parent, false)
        UserViewHolder(bindingUser, onClickItemListener)
      }

      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (getItemViewType(position)) {
      SHOW_PROMPT -> (holder as TextViewViewHolder).bind(items[position] as String)
      SHOW_DIVIDER -> (holder as DividerViewHolder).bind()
      SHOW_USER -> (holder as UserViewHolder).bind(items[position] as User)
    }
  }

  override fun getItemCount(): Int = items.size

  // Submit sorted items
  fun submitItems(orderItems: MutableList<Any?>) {
    this.items.clear()
    this.items.addAll(orderItems)
    notifyDataSetChanged()
  }

  fun clearItems() {
    this.items.clear()
    notifyDataSetChanged()
  }

  // Set listener for posts and recipes
  fun setItemListener(onClickItemListener: OnClickItemListener) {
    this.onClickItemListener = onClickItemListener
  }
}