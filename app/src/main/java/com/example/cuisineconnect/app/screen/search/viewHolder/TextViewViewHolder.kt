package com.example.cuisineconnect.app.screen.search.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.OnClickItemListener
import com.example.cuisineconnect.databinding.ItemPostTextViewBinding
import com.example.cuisineconnect.databinding.ItemSearchTextViewBinding

class TextViewViewHolder(
    private val view: ItemSearchTextViewBinding,
    private val itemListener: OnClickItemListener?
) : RecyclerView.ViewHolder(view.root) {

    fun bind(prompt: String) {
        view.run {
            tvUserInput.run {
                text = prompt
                setOnClickListener {
                    itemListener?.onPromptClicked(prompt)
                }
            }
        }
    }
}