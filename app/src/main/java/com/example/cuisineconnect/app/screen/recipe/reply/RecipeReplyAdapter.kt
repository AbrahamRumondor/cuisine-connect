package com.example.cuisineconnect.app.screen.recipe.reply

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.app.screen.recipe.reply.viewHolder.RecipeReplyChildViewHolder
import com.example.cuisineconnect.app.screen.recipe.reply.viewHolder.RecipeReplyRootViewHolder
import com.example.cuisineconnect.databinding.ReplyRecipeChildBinding
import com.example.cuisineconnect.databinding.ReplyRecipeRootBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User

class RecipeReplyAdapter :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var items: MutableList<Pair<User?, Reply>> = mutableListOf()
  private var recipeReplyItemListener: RecipeReplyItemListener? = null

  private val ROOT_REPLY = 0
  private val CHILD_REPLY = 1

  override fun getItemViewType(position: Int): Int {
    return items[position].second.isRoot
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)

    return when (viewType) {
      ROOT_REPLY -> {
        val bindingRootReply = ReplyRecipeRootBinding.inflate(inflater, parent, false)
        RecipeReplyRootViewHolder(bindingRootReply, recipeReplyItemListener)
      }

      CHILD_REPLY -> {
        val bindingChildReply = ReplyRecipeChildBinding.inflate(inflater, parent, false)
        RecipeReplyChildViewHolder(bindingChildReply, recipeReplyItemListener)
      }

      else -> throw IllegalArgumentException("Invalid view type")
    }
  }


  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = items[position]
    when (items[position].second.isRoot) {

      ROOT_REPLY -> (holder as RecipeReplyRootViewHolder).bind(position, item.first, item.second)

      CHILD_REPLY -> (holder as RecipeReplyChildViewHolder).bind(position, item.first, item.second)

      else -> throw IllegalArgumentException("Invalid item type")
    }
  }


  override fun getItemCount(): Int {
    return items.size
  }

  fun submitReplies(newReplies: MutableList<Pair<User?, Reply>>) {
    val startIndex = items.indexOfFirst { currentItem ->
      newReplies.none { newItem -> newItem.second.id == currentItem.second.id }
    }

    if (startIndex == -1) {
      this.items.clear()
      this.items.addAll(newReplies)
      notifyDataSetChanged()
      return
    }

    this.items.clear()
    this.items.addAll(newReplies)

    notifyItemRangeChanged(startIndex, newReplies.size - startIndex)
  }

  // Add new replies at a specific position
  fun addRepliesAtPosition(position: Int, newReplies: List<Pair<User?, Reply>>) {
    // Insert new replies right after the position provided
    items.addAll(position + 1, newReplies)
    notifyItemRangeInserted(position + 1, newReplies.size)
  }

  // Update upvotes for a reply at a specific position
  fun updateReplyAtPosition(position: Int, updatedReply: Reply) {
    val pair = items[position]
    items[position] = pair.copy(second = updatedReply)
    notifyItemChanged(position)
  }

  fun setItemListener(recipeReplyItemListener: RecipeReplyItemListener) {
    this.recipeReplyItemListener = recipeReplyItemListener
  }
}