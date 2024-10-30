package com.example.cuisineconnect.app.screen.search.trending

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.databinding.HashtagTrendingListBinding
import com.example.cuisineconnect.databinding.SubTitleBinding
import com.example.cuisineconnect.domain.model.Hashtag

class TrendingHashtagAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<Any> = mutableListOf()

  private var onClickItemListener: OnClickItemListener? = null

  companion object {
    private const val VIEW_TYPE_HEADER = 0
    private const val VIEW_TYPE_HASHTAG = 1
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is String -> VIEW_TYPE_HEADER
      is Hashtag -> VIEW_TYPE_HASHTAG
      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_HEADER -> {
        val binding = SubTitleBinding.inflate(inflater, parent, false)
        TrendingHeaderViewHolder(binding)
      }
      VIEW_TYPE_HASHTAG -> {
        val binding = HashtagTrendingListBinding.inflate(inflater, parent, false)
        HashtagViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = items[position]) {
      is String -> (holder as TrendingHeaderViewHolder).bind(item)
      is Hashtag -> (holder as HashtagViewHolder).bind(item, onClickItemListener)
      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  override fun getItemCount(): Int = items.size

  fun submitData(newItems: List<Any>) {
    items.clear()
    items.add("Trending Today")
    items.addAll(newItems)
    notifyDataSetChanged()
  }

  fun setItemListener(onClickItemListener: OnClickItemListener) {
    this.onClickItemListener = onClickItemListener
  }
}