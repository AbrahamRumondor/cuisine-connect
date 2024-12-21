package com.example.cuisineconnect.app.screen.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.R
import com.example.cuisineconnect.domain.model.Hashtag

class HashtagAdapter(
  private val hashtagList: MutableList<Hashtag>,
  private val onClick: (Hashtag) -> Unit // For item clicks, if needed
) : RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder>() {

  inner class HashtagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val hashtagBody: TextView = itemView.findViewById(R.id.tv_tags_body)
    val hashtagCount: TextView = itemView.findViewById(R.id.tv_tags_count)

    fun bind(hashtag: Hashtag) {
      hashtagBody.text = "${hashtag.body}"
      val usages = itemView.context.getString(R.string.usages)
      hashtagCount.text = if (hashtag.listId.isNotEmpty()) {
        "${hashtag.listId.size} $usages"
      } else {
        "→"
      }

      // Handle item click, if needed
      itemView.setOnClickListener {
        onClick(hashtag)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag_list, parent, false)
    return HashtagViewHolder(view)
  }

  override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
    val hashtag = hashtagList[position]
    holder.bind(hashtag)
  }

  override fun getItemCount(): Int {
    return hashtagList.size
  }

  fun updateHashtags(newHashtags: List<Hashtag>) {
    hashtagList.clear()
    hashtagList.addAll(newHashtags)
    notifyDataSetChanged() // Refresh the RecyclerView
  }
}