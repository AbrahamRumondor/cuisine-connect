package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.databinding.RecipeDetailStepBinding
import com.example.cuisineconnect.domain.model.Step

class RecipeDetailStepViewHolder(
    private val view: RecipeDetailStepBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(step: Step?) {
        view.run {
            if (step != null) {

                tvNo.text = step.no.toString()
                tvContent.text = step.body

//                Glide.with(root)
//                    .load(menu.image)
//                    .placeholder(android.R.drawable.ic_menu_report_image)
//                    .into(imgMenu)
            }
        }
    }
}