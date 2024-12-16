package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.databinding.RecipeDetailStepBinding
import com.example.cuisineconnect.databinding.SubTitleBinding
import com.example.cuisineconnect.domain.model.Step

class RecipeDetailSubTitleViewHolder(
  private val view: SubTitleBinding,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(string: String) {
    view.run {
      if (string.isNotEmpty()) {
        root.textSize = 24f
        root.text = string
        root.setTypeface(root.typeface, android.graphics.Typeface.BOLD) // Set text to bold
      }
    }
  }
}