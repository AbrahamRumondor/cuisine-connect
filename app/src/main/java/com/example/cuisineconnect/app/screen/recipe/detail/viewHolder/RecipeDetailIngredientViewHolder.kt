package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.databinding.RecipeDetailIngredientBinding
import com.example.cuisineconnect.domain.model.Ingredient

class RecipeDetailIngredientViewHolder(
    private val view: RecipeDetailIngredientBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(string: String?) {
        view.run {
            if (string != null) {
                tvIngredient.text = string
            }
        }
    }
}