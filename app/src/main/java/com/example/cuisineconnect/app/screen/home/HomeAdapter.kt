package com.example.cuisineconnect.app.screen.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemPostViewHolder
import com.example.cuisineconnect.app.screen.profile.post.viewHolder.ItemRecipeViewHolder
import com.example.cuisineconnect.data.pagingSource.FeedItem
import com.example.cuisineconnect.data.pagingSource.FeedItem.PostItem
import com.example.cuisineconnect.data.pagingSource.FeedItem.RecipeItem
import com.example.cuisineconnect.databinding.ItemPostHorizontalBinding
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding

class HomeAdapter : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemComparator) {

    private var postNRecipeItemListener: ItemListListener? = null
    private var createPostViewModel: CreatePostViewModel? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FeedItem.PostItem -> VIEW_TYPE_POST
            is FeedItem.RecipeItem -> VIEW_TYPE_RECIPE
            else -> throw IllegalArgumentException("Unknown view type at position: $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_POST -> {
                val bindingPost = ItemPostHorizontalBinding.inflate(inflater, parent, false)
                ItemPostViewHolder(bindingPost)
            }
            VIEW_TYPE_RECIPE -> {
                val bindingRecipe = ItemRecipeHorizontalBinding.inflate(inflater, parent, false)
                ItemRecipeViewHolder(bindingRecipe)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("homeAdapter", " item ${getItem(position)}")
        when (val item = getItem(position)) {
            is PostItem -> {
                (holder as ItemPostViewHolder).bind(
                    item.user,
                    item.post,
                    postNRecipeItemListener,
                    createPostViewModel
                )
            }
            is RecipeItem -> {
                (holder as ItemRecipeViewHolder).bind(
                    item.user,
                    item.recipe,
                    postNRecipeItemListener,
                    createPostViewModel
                )
            }
            null -> {
                // Properly handle null case
                // Consider logging or throwing an exception if this case shouldn't happen.
                println("Error: item at position $position is null.")
            }
        }
    }

    fun setItemListener(postNRecipeItemListener: ItemListListener) {
        this.postNRecipeItemListener = postNRecipeItemListener
    }

    fun addViewModel(createPostViewModel: CreatePostViewModel) {
        this.createPostViewModel = createPostViewModel
    }

    companion object {
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_RECIPE = 1

        val FeedItemComparator = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                return when {
                    oldItem is FeedItem.PostItem && newItem is FeedItem.PostItem -> oldItem.post.id == newItem.post.id
                    oldItem is FeedItem.RecipeItem && newItem is FeedItem.RecipeItem -> oldItem.recipe.id == newItem.recipe.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                return when {
                    oldItem is FeedItem.PostItem && newItem is FeedItem.PostItem -> oldItem.post == newItem.post
                    oldItem is FeedItem.RecipeItem && newItem is FeedItem.RecipeItem -> oldItem.recipe == newItem.recipe
                    else -> false
                }
            }
        }
    }
}