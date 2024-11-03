package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import android.app.Dialog
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.RecipeDetailEstimationBinding
import com.example.cuisineconnect.domain.model.Recipe

class RecipeDetailEstimationViewHolder(
    private val view: RecipeDetailEstimationBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(recipe: Recipe?) {
        view.run {
            if (recipe != null) {
                tvDuration.text = recipe.duration.toString()
                tvPortion.text = recipe.portion.toString()
            }
        }
    }
}