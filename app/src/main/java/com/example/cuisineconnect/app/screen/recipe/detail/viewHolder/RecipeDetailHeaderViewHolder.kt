package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.databinding.RecipeDetailHeaderBinding
import com.example.cuisineconnect.domain.model.Recipe

class RecipeDetailHeaderViewHolder(
    private val view: RecipeDetailHeaderBinding,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(recipe: Recipe?) {
        view.run {
            if (recipe != null) {
                tvTitle.text = recipe.title
                tvDescription.text = recipe.description
            }
        }
    }
}