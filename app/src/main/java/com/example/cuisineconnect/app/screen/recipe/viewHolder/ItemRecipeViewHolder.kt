package com.example.cuisineconnect.app.screen.recipe.viewHolder

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.databinding.ItemRecipeBigImageBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ItemRecipeViewHolder(
  private val view: ItemRecipeBigImageBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(
    user: User?,
    recipe: Recipe?,
    listener: ItemListListener?,
    createPostViewModel: CreatePostViewModel?
  ) {
    view.run {
      if (user != null && recipe != null) {

        Log.d("lilil", "this is recipe ${user} and ${recipe}")

        tvUsername.text = user.name

        Glide.with(root)
          .load(user.image)
          .placeholder(R.drawable.ic_bnv_profile)
          .into(ivUserProfile)

        tvTitle.text = recipe.title
//        tvDesc.text = recipe.description

        val formattedDate = getRelativeTime(recipe.date) // recipe.date is of type Date
        tvDate.text = formattedDate

        cvRecipe.setOnClickListener {
          listener?.onRecipeClicked(recipe.id)
          Toast.makeText(
            view.root.context,
            "you clicked ${recipe.title}",
            Toast.LENGTH_SHORT
          ).show()
        }

        cvRecipe.setOnLongClickListener {
          listener?.onRecipeLongClicked(recipe.id)
          Toast.makeText(
            view.root.context,
            "you long clicked ${recipe.title}",
            Toast.LENGTH_SHORT
          ).show()
          true
        }

        val isAuthor = createPostViewModel?.user?.value?.id == user.id
        btnDelete.visibility = View.INVISIBLE
        if (isAuthor) {
          btnDelete.visibility = View.VISIBLE
          btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(view.root.context)
            builder.setTitle("Delete Recipe")
            builder.setMessage("Are you sure you want to delete the recipe?")

            builder.setPositiveButton("Yes") { dialog, _ ->
              Toast.makeText(
                view.root.context,
                "${recipe.title} deleted",
                Toast.LENGTH_SHORT
              ).show()

              listener?.onItemDeleteClicked(recipe.id, "recipe")
              dialog.dismiss() // Dismiss the dialog
            }

            builder.setNegativeButton("No") { dialog, _ ->
              dialog.dismiss()
            }

            builder.create().show()
          }
        }
        val uri = Uri.parse(recipe.image)
        Glide.with(view.root)
          .load(uri)   // Load the image URL into the ImageView
          .placeholder(R.drawable.ic_no_image)
          .into(ivImage)

//        if (isMyRecipes) holder.editRecipe.visibility = View.VISIBLE
//        else holder.editRecipe.visibility = View.GONE

        tvUpvoteCount.text = recipe.upvotes.size.toString()
        tvReplyCount.text = recipe.replyCount.toString()
        tvBookmarkCount.text = recipe.bookmarks.size.toString()

//                ivOrderAdd.setOnClickListener {
//                    itemListener?.onAddItemClicked(position, menu.id)
//                }
      }
    }
  }

  private fun getRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val timestamp = date.time
    val diff = now - timestamp

    return when {
      // Less than 1 minute ago
      diff < TimeUnit.MINUTES.toMillis(1) -> {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds <= 1) "$seconds second ago" else "$seconds seconds ago"
      }

      // Less than 1 hour ago
      diff < TimeUnit.HOURS.toMillis(1) -> {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes == 1L) "$minutes minute ago" else "$minutes minutes ago"
      }

      // Less than 1 day ago
      diff < TimeUnit.HOURS.toMillis(24) -> {
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours == 1L) "$hours hour ago" else "$hours hours ago"
      }

      // Less than 5 days ago
      diff < TimeUnit.DAYS.toMillis(5) -> {
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days == 1L) "$days day ago" else "$days days ago"
      }

      // Default to the "MMM dd" format for anything older
      else -> {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateFormat.format(date)
      }
    }
  }
}