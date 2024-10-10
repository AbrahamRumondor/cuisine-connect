package com.example.cuisineconnect.app.screen.profile.post.viewHolder

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import java.text.SimpleDateFormat

class ItemRecipeViewHolder(
  private val view: ItemRecipeHorizontalBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(user: User?, recipe: Recipe?, listener: RecipeListListener?) {
    view.run {
      if (user != null && recipe != null) {

        Log.d("lilil", "this is recipe ${user} and ${recipe}")

        tvUsername.text = user.name

        Glide.with(root)
          .load(user.image)
          .placeholder(android.R.drawable.ic_menu_report_image)
          .into(ivUserProfile)

        tvTitle.text = recipe.title
        tvDesc.text = recipe.description

        val dateFormat = SimpleDateFormat("MMM dd")
        val formattedDate = dateFormat.format(recipe.date)

        tvDate.text = formattedDate
        clRecipeItem.setOnClickListener {
          listener?.onRecipeClicked(recipe.id)
          Toast.makeText(
            view.root.context,
            "you touched ${recipe.title} 😳",
            Toast.LENGTH_SHORT
          ).show()
        }

        clRecipeItem.setOnLongClickListener {
          listener?.onRecipeLongClicked(recipe.id)
          Toast.makeText(
            view.root.context,
            "you long touched ${recipe.title} 😳😳😳",
            Toast.LENGTH_SHORT
          ).show()
          true
        }
        btnEdit.setOnClickListener {
          Toast.makeText(
            view.root.context,
            "${recipe.title} edited",
            Toast.LENGTH_SHORT
          ).show()
        }
        val uri = Uri.parse(recipe.image)
        Glide.with(view.root)
          .load(uri)   // Load the image URL into the ImageView
          .placeholder(android.R.drawable.ic_menu_report_image)
          .into(ivImageTitle)

//        if (isMyRecipes) holder.editRecipe.visibility = View.VISIBLE
//        else holder.editRecipe.visibility = View.GONE

        tvUpvoteCount.text = recipe.upvotes.size.toString()
        tvReplyCount.text = recipe.replyCount.toString()
        tvBookmarkCount.text = recipe.bookmarkCount.toString()

//                ivOrderAdd.setOnClickListener {
//                    itemListener?.onAddItemClicked(position, menu.id)
//                }
      }
    }
  }
}