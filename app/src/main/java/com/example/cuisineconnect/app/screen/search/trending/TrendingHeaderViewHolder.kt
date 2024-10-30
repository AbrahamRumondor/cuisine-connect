package com.example.cuisineconnect.app.screen.search.trending

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.databinding.SubTitleBinding

class TrendingHeaderViewHolder(
    private val view: SubTitleBinding,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(string: String) {
        view.run {
            if (string.isNotEmpty()) {
                root.text = string
            }
        }
    }
}