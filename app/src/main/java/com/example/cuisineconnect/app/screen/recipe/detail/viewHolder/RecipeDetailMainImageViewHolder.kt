package com.example.cuisineconnect.app.screen.recipe.detail.viewHolder

import android.app.Dialog
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.RecipeDetailEstimationBinding
import com.example.cuisineconnect.databinding.RecipeDetailMainImageBinding
import com.example.cuisineconnect.domain.model.Recipe

class RecipeDetailMainImageViewHolder(
    private val view: RecipeDetailMainImageBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

    fun bind(recipe: Recipe?) {
        view.run {
            if (recipe != null) {
                Glide.with(root)
                    .load(recipe.image)
                    .placeholder(R.drawable.recipe_img)
                    .into(ivRecipeImg)

                ivRecipeImg.setOnClickListener {
                    if (recipe.image == "null") {
                        Toast.makeText(root.context, root.context.getString(R.string.no_image), Toast.LENGTH_SHORT).show()
                    } else {
                        // Create and display a dialog to show the full image
                        val dialog = Dialog(view.root.context)
                        dialog.setContentView(R.layout.dialog_full_image)

                        val fullImageView = dialog.findViewById<ImageView>(R.id.iv_full_image)
                        Glide.with(view.root).load(recipe.image)
                            .placeholder(R.drawable.loading_image).into(fullImageView)

                        // Set dialog layout parameters to fill 90-95% of the screen
                        val params = dialog.window?.attributes
                        params?.width =
                            (view.root.context.resources.displayMetrics.widthPixels * 0.90).toInt() // 95% of screen width
                        params?.height =
                            (view.root.context.resources.displayMetrics.heightPixels * 0.90).toInt() // 95% of screen height
                        dialog.window?.attributes = params

                        val closeView = dialog.findViewById<ImageButton>(R.id.btn_close)
                        closeView.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()
                    }
                }
            }
        }
    }
}