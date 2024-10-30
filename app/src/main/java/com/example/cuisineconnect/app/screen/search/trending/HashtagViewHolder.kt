package com.example.cuisineconnect.app.screen.search.trending

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.databinding.HashtagTrendingListBinding
import com.example.cuisineconnect.domain.model.Hashtag

class HashtagViewHolder (
    private val view: HashtagTrendingListBinding,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(hashtag: Hashtag?, onClickItemListener: OnClickItemListener?) {
        view.run {
            if (hashtag != null) {
                tvHashtagBody.text = hashtag.body
                val hashtagCount = hashtag.listId.size.toString() + " posts"
                tvHashtagsCount.text = hashtagCount

                view.root.setOnClickListener {
                    onClickItemListener?.onPromptClicked(hashtag.body)
                }
            }
        }
    }
}